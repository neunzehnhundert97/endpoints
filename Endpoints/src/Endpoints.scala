package de.neunzehnhundert97
package endpoints

/** An endpoint agreement where the client sends A and receives B. */
trait Endpoint[+A, -B]:
  /** The HTTP path this endpoint is provided / invoked at. */
  def path: String = s"/api/${toString}"
  def method: Method

object Endpoint {
  inline def apply[A, B](inline pathArg: String, inline methodArg: Method = Method.POST): Endpoint[A, B] =
    new {
      override def path   = pathArg
      override def method = methodArg
    }
}
