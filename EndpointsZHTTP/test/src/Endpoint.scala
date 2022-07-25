import de.neunzehnhundert97.endpoints.zhttp.*
import de.neunzehnhundert97.endpoints.{Method => ME, *}

import zio.ZIO
import zio.Console

import zio.test.*

object E extends Endpoint[Long, String] {
  def method = ME.POST
}

object ZHTTPEndpointSpec extends ZIOSpecDefault {
  def spec = suite("ZHTTP Endpoint")(
    suite("creation")(
      test("ZIO function (direct)") {
        EndpointCreator(E).create(l => ZIO.succeed(l.toString))
        assertTrue(true)
      },
      test("ZIO function (implicit)") {
        EndpointCreator(E).createFrom((l: Long) => ZIO.succeed(l.toString))
        assertTrue(true)
      },
      test("ZIO (direct)") {
        EndpointCreator(E).createFromZIO(ZIO.succeed("Long"))
        assertTrue(true)
      },
      test("ZIO (implicit)") {
        EndpointCreator(E).createFrom(ZIO.succeed("Long"))
        assertTrue(true)
      },
      test("Function (direct)") {
        EndpointCreator(E).createFromFunction((l: Long) => l.toString)
        assertTrue(true)
      },
      test("Function (implicit)") {
        EndpointCreator(E).createFrom((l: Long) => l.toString)
        assertTrue(true)
      },
      test("Static (direct)") {
        EndpointCreator(E).createFromStatic("Long")
        assertTrue(true)
      },
      test("Static (implicit)") {
        EndpointCreator(E).createFrom("Long")
        assertTrue(true)
      }
    )
  )
}
