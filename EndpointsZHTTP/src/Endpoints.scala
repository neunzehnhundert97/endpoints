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
    inline function: A => ZIO[R, E, B]
  ): Http[ParseLocker & R, Any, Request, Response] = {
    // Get method and path
    // This must be done before the matching as matching does not allow for expressions
    val M = Method.fromString(endpoint.method.toString)
    val P = Path(endpoint.path)

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

  /** Generates a route for the given endpoint. */
  inline def createFromStatic(b: B) =
    create(_ => ZIO.succeed(b))

  /** Generates a route for the given endpoint. */
  inline def createFromZIO[R, E](zio: ZIO[R, E, B]) =
    create(_ => zio)

  /** Generates a route for the given endpoint. */
  def createFromFunction(func: A => B) =
    create(func.andThen(ZIO.succeed))

  /** Perform parsing of the JSON string fibre safe. */
  def parseSafely(str: String) =
    for
      sem <- ParseLocker.parsing
      res <- sem.withPermit(ZIO.attempt(read[A](str)).mapError(t => ParsingException(t.getMessage)))
    yield res

  /** Generate a route for the given endpoint. This function accepts all possible inputs. */
  inline def createFrom[Input, R, E](inline input: Input)(using ev: EPConstructor[A, B, R, E, Input]) =
    create(ev.convert(input))
}

trait EPConstructor[In, Out, R, E, Convertee] {
  def convert(input: Convertee): In => ZIO[R, E, Out]
}

implicit def zioFunctionConstructor[A, B, R, E]: EPConstructor[A, B, R, E, A => ZIO[R, E, B]] =
  new EPConstructor[A, B, R, E, A => ZIO[R, E, B]] {
    def convert(input: A => ZIO[R, E, B]): A => ZIO[R, E, B] =
      input
  }

implicit def functionConstructor[A, B]: EPConstructor[A, B, Any, Nothing, A => B] =
  new EPConstructor[A, B, Any, Nothing, A => B] {
    def convert(input: A => B): A => ZIO[Any, Nothing, B] =
      input.andThen(ZIO.succeed)
  }

implicit def zioConstructor[A, B, R, E]: EPConstructor[A, B, R, E, ZIO[R, E, B]] =
  new EPConstructor[A, B, R, E, ZIO[R, E, B]] {
    def convert(input: ZIO[R, E, B]): A => ZIO[R, E, B] =
      _ => input
  }

implicit def staticConstructor[A, B]: EPConstructor[A, B, Any, Nothing, B] =
  new EPConstructor[A, B, Any, Nothing, B] {
    def convert(input: B): A => ZIO[Any, Nothing, B] =
      _ => ZIO.succeed(input)
  }
