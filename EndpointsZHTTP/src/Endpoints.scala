package de.neunzehnhundert97.endpoints.zhttp

import zio.{Has, ZIO}
import zio.console.{Console, putStrLn}
import zio.console.Console.Service

import scala.compiletime.{erasedValue, summonInline}
import scala.annotation.implicitNotFound

import java.nio.charset.Charset

import zhttp.http.*

import upickle.default.{write, Writer, Reader, read}

import de.neunzehnhundert97.endpoints.{Endpoint => SEP}

object ServerAdapter {

  /** Generates an route for the given endpoint, which calls the given function. */
  inline def generateEndpoint[A: Reader, B: Writer, B1, R, E](
    inline e: SEP[A, B],
    inline func: A => ZIO[R, E, B1]
  )(using
    @implicitNotFound("The return ${B1} does not conform to ${B}") inline ev: B1 <:< B
  ): Http[Has[ParseLocker] & Console & R, Any, Request, Response] = {
    // ) = {
    // Get method and path
    // This must be done before the matching as matching does not allow for expressions
    val M = Method.fromString(e.method.toString)
    val P = Path(e.path)

    // Match the request
    Http.collectZIO[Request] {
      case req @ M -> P =>
        // Check the input type of the endpoint
        val data = inline erasedValue[A] match {
          // If it is unit, skip reading the body and return a typed unit
          case _: Unit =>
            val ev = summonInline[Unit =:= A]
            ZIO.succeed(ev(()))
          // For anything else, read the request body and parse it as JSON
          case _ => for {
              body   <- req.bodyAsString
              parsed <- parseSafely[A](body)
            } yield parsed
        }

        for {
          d   <- data
          _   <- putStrLn(s"$M: $P").ignore
          res <- func(d)
        } yield Response.json(write(ev(res)))
    }
  }

  /** Due to unknown reasons, the JSON-parsing is not fibre safe, so it is protected with a mutex. */
  def parseSafely[A: Reader](str: String) = for {
    sem <- ParseLocker.parsing
    res <- sem.withPermit(ZIO.effect(read[A](str)).mapError(t => ParsingException(t.getMessage)))
  } yield res

  /** Generates an route for the given endpoint, which performs the given effect. */
  inline def generateEndpoint[B: Writer, B1, R, E](
    inline e: SEP[Unit, B],
    inline func: ZIO[R, E, B1]
  )(using
    @implicitNotFound("The return ${B1} does not conform to ${B}") inline ev: B1 <:< B
  ): Http[Has[ParseLocker] & Console & R, Any, Request, Response] =
    generateEndpoint(e, _ => func)
}
