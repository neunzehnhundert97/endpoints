import de.neunzehnhundert97.endpoints.zhttp.*
import de.neunzehnhundert97.endpoints.{Method => ME, *}

import zio.ZIO
import zio.Console

import zio.test.*

object E extends Endpoint[Long, Int] {
  def method = ME.POST
}

def aa(l: Long)            = l.toInt
def aaa(l: Long)           = ZIO.succeed(l.toInt)
def bb: ZIO[Any, Int, Int] = ZIO.fail(1)

object ZHTTPEndpointSpec extends ZIOSpecDefault {
  def spec = suite("Stuff")(
    test("Bla") {
      EndpointCreator(E).experiment(aa)
      EndpointCreator(E).experiment(aaa)
      EndpointCreator(E).experiment(bb)
      EndpointCreator(E).experiment(1)
      assertTrue(true)
    }
  )
}
