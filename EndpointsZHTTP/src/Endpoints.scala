package de.neunzehnhundert97.endpoints.zhttp

import scala.annotation.implicitNotFound
import scala.compiletime.{codeOf, erasedValue, error, summonFrom, summonInline}

import zio.{Console, ZIO}

import upickle.default.{Reader, Writer, read, write}
import zhttp.http.*

import de.neunzehnhundert97.endpoints.{Endpoint => SEP}

final case class EndpointCreator[A: Reader, B: Writer](endpoint: SEP[A, B]) {

  /** Generates an route for the given endpoint. */
  inline def create[R, E](
    inline func: (A => ZIO[R, E, B]) | ZIO[R, E, B] |(A => B) | B
  ): Http[ParseLocker & R, Any, Request, Response] = {
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
          _   <- Console.printLine(s"$M: $P").ignore
          res <- function(d)
        yield Response.json(write(res))
    }
  }

  /** Perform parsing of the JSON string fibre safe. */
  def parseSafely(str: String) =
    for
      sem <- ParseLocker.parsing
      res <- sem.withPermit(ZIO.attempt(read[A](str)).mapError(t => ParsingException(t.getMessage)))
    yield res

}
