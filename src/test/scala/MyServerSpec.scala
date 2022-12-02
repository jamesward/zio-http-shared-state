import zio.http.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*

object MyServerSpec extends ZIOSpecDefault:

  def spec = suite("server")(
    test("should count one") {
      for
        resp <- MyServer.app(Request.get(URL.root)).provideLayer(MyServer.sharedStateLayer)
        body <- resp.body.asString
      yield
        assert(body)(equalTo("Count = 1"))
    },
    test("should count two sequential") {
      val check = for
        _ <- MyServer.app(Request.get(URL.root))
        resp <- MyServer.app(Request.get(URL.root))
        body <- resp.body.asString
      yield
        assert(body)(equalTo("Count = 2"))

      check.provideLayer(MyServer.sharedStateLayer)
    },
    test("should count two parallel") {
      val check = for
        oneFork <- MyServer.app(Request.get(URL.root)).fork
        twoFork <- MyServer.app(Request.get(URL.root)).fork
        _ <- oneFork.join
        two <- twoFork.join
        body <- two.body.asString
      yield
        assert(body)(equalTo("Count = 2"))

      check.provideLayer(MyServer.sharedStateLayer)
    },
  )
