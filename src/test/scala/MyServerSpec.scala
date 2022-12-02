import zio.Ref
import zio.http.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*

object MyServerSpec extends ZIOSpecDefault:

  def spec = suite("server")(
    test("should count one") {
      for
        ref <- MyServer.sharedState
        resp <- MyServer.app(ref)(Request.get(URL.root))
        body <- resp.body.asString
      yield
        assert(body)(equalTo("Count = 1"))
    },
    test("should count two sequential") {
      for
        ref <- MyServer.sharedState
        _ <- MyServer.app(ref)(Request.get(URL.root))
        resp <- MyServer.app(ref)(Request.get(URL.root))
        body <- resp.body.asString
      yield
        assert(body)(equalTo("Count = 2"))
    },
    test("should count two parallel") {
      for
        ref <- MyServer.sharedState
        oneFork <- MyServer.app(ref)(Request.get(URL.root)).fork
        twoFork <- MyServer.app(ref)(Request.get(URL.root)).fork
        _ <- oneFork.join
        two <- twoFork.join
        body <- two.body.asString
      yield
        assert(body)(equalTo("Count = 2"))
    },
  )
