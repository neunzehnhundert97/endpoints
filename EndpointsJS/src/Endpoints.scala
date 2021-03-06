package de.neunzehnhundert97.endpoints.js

import scala.compiletime.{erasedValue, summonInline}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

import org.scalajs.dom.experimental.HttpMethod

import io.laminext.fetch.Fetch
import io.laminext.fetch.upickle._

import upickle.default.{read, Reader, Writer, write}

import com.raquo.airstream.core.EventStream

import org.scalajs.dom.ext.Ajax

import de.neunzehnhundert97.endpoints.{Endpoint, Method}

trait ClientEndpoint[F[_], A, B]:

  /** Calls the endpoint and gives the result in an event stream. */
  def call(input: A): F[B]

  /** Calls the endpoint and gives the result in an event stream. */
  def apply(input: A): F[B] =
    call(input)

  /** Calls the endpoint and gives the result in an event stream. */
  def apply(input: A)(handler: PartialFunction[Try[B], Unit])(implicit ev: F[B] =:= Future[B]): Unit =
    ev(call(input)).onComplete(handler)

trait ClientEndpointWithoutParamter[F[_], A, B] extends ClientEndpoint[F, A, B]:

  def ev: Unit =:= A

  def call(): F[B] =
    call(ev(()))

  def apply(): F[B] =
    apply(ev(()))

final case class EndpointCreator[A: Writer, B: Reader](endpoint: Endpoint[A, B]) {

  /** Builds a callable endpoint from the abstract describtion. */
  transparent inline def createLaminextEndpoint =
    val method = endpoint.method.toString.asInstanceOf[HttpMethod]

    inline erasedValue[A] match
      case _: Unit =>
        new ClientEndpointWithoutParamter[EventStream, A, B]:
          override def call(input: A): EventStream[B] =
            val resp = Fetch(method, s"./${endpoint.path}").text
            inline erasedValue[B] match
              case _: Unit =>
                val ev = summonInline[Unit =:= B]
                resp.mapToStrict(ev(()))
              case _ => resp.map(r => read[B](r.data, true))

          override def ev = summonInline[Unit =:= A]
        end new

      case _ =>
        new ClientEndpoint[EventStream, A, B]:
          override def call(input: A): EventStream[B] =
            val resp = Fetch(method, s"./${endpoint.path}", body = input).text
            inline erasedValue[B] match
              case _: Unit =>
                val ev = summonInline[Unit =:= B]
                resp.mapToStrict(ev(()))
              case _ => resp.map(r => read[B](r.data))

        end new

  /** Builds a callable endpoint from the abstract describtion. */
  transparent inline def createFetchEndpoint =
    val method = endpoint.method.toString.asInstanceOf[HttpMethod]

    inline erasedValue[A] match
      case _: Unit =>
        new ClientEndpointWithoutParamter[Future, A, B]:
          override def call(input: A): Future[B] =
            val resp = Ajax(endpoint.method.toString, s"./${endpoint.path}", null, 0, Map.empty, false, "")
            inline erasedValue[B] match
              case _: Unit =>
                val ev = summonInline[Unit =:= B]
                resp.map(_ => ev(()))
              case _ => resp.map(r => read[B](r.responseText))

          override def ev = summonInline[Unit =:= A]
        end new

      case _ =>
        new ClientEndpoint[Future, A, B]:
          override def call(input: A): Future[B] =
            val resp = Ajax(endpoint.method.toString, s"./${endpoint.path}", write(input), 0, Map.empty, false, "")
            inline erasedValue[B] match
              case _: Unit =>
                val ev = summonInline[Unit =:= B]
                resp.map(_ => ev(()))
              case _ => resp.map(r => read[B](r.responseText))

        end new
}
