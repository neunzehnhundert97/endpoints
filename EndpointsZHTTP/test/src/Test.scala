import de.neunzehnhundert97.endpoints.zhttp.*
import de.neunzehnhundert97.endpoints.{Method => ME, *}

import zio.ZIO
import zio.console.putStr

import zio.test.*
import zio.test.Assertion.*
import zio.test.environment.*
import java.util.Date

case class Wrapper[A](a: A)

case object E extends Endpoint[Int, Long] {
  def method = ME.GET
  def path   = "path"
}

object ZHTTPEndpointSpec extends DefaultRunnableSpec {
  def spec = suite("Stuff")(
    test("Bla") {
      // EndpointCreator(E).create(putStr)
      assertTrue(true)
    }
  )

  val a = List(1, 1L)

}
