package de.neunzehnhundert97.endpoints.zhttp

import zio.{Has, ZIO}
import zio.console.{Console, putStrLn}
import zio.console.Console.Service

import scala.compiletime.{erasedValue, summonInline, summonFrom, error, codeOf}
import scala.annotation.implicitNotFound

import zhttp.http.*

import upickle.default.{write, Writer, Reader, read}

import de.neunzehnhundert97.endpoints.{Endpoint => SEP}

final case class EndpointCreator[A: Reader, B: Writer](endpoint: SEP[A, B]) {

  /** Generates an route for the given endpoint. */
  inline def create[R, E](
    inline func: (A => ZIO[R, E, B]) | ZIO[R, E, B] |(A => B) | B
  ): Http[Has[ParseLocker] & Console & R, Any, Request, Response] = {
    // Get method and path
    // This must be done before the matching as matching does not allow for expressions
    val M = Method.fromString(endpoint.method.toString)
    val P = Path(endpoint.path)

    // Convert the given function into the right form
    val function: A => ZIO[R, E, B] = inline func.match {
      case a: B                         => (_: A) => ZIO.succeed(a)
      case a: ZIO[R, E, B]              => (_: A) => a
      case a: Function[A, ZIO[R, E, B]] => a
      case a: Function[A, B]            => a.andThen(ZIO.succeed)
    }

    // Match the request
    Http.collectZIO[Request] {
      case req @ M -> P =>
        // Check the input type of the endpoint
        val data =
          inline erasedValue[A] match
            // If it is unit, skip reading the body and return a typed unit
            case _: Unit =>
              val ev = summonInline[Unit =:= A]
              ZIO.succeed(ev(()))
            // For anything else, read the request body and parse it as JSON
            case _ =>
              for
                body   <- req.bodyAsString
                parsed <- parseSafely(body)
              yield parsed

        for
          d   <- data
          _   <- putStrLn(s"$M: $P").ignore
          res <- function(d)
        yield Response.json(write(res))
    }
  }

  /** Perform parsing of the JSON string fibre safe. */
  def parseSafely(str: String) =
    for
      sem <- ParseLocker.parsing
      res <- sem.withPermit(ZIO.effect(read[A](str)).mapError(t => ParsingException(t.getMessage)))
    yield res

}
