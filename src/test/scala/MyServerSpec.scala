import zio.Ref
import zio.http.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*

object MyServerSpec extends ZIOSpecDefault:

  def spec = suite("server")(
    test("should count one") {
      for
        ref <- Ref.Synchronized.make(0)
        resp <- MyServer.app(ref)(Request.get(URL.root))
        body <- resp.body.asString
      yield
        assert(body)(equalTo("Count = 1"))
    },
    test("should count two sequential") {
      val check = for
        ref <- Ref.Synchronized.make(0)
        _ <- MyServer.app(ref)(Request.get(URL.root))
        resp <- MyServer.app(ref)(Request.get(URL.root))
        body <- resp.body.asString
      yield
        assert(body)(equalTo("Count = 2"))

      check.provideLayer(MyServer.sharedStateLayer)
    },
    test("should count two parallel") {
      for
        ref <- Ref.Synchronized.make(0)
        oneFork <- MyServer.app(ref)(Request.get(URL.root)).fork
        twoFork <- MyServer.app(ref)(Request.get(URL.root)).fork
        _ <- oneFork.join
        two <- twoFork.join
        body <- two.body.asString
      yield
        assert(body)(equalTo("Count = 2"))
    },
  )
