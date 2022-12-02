import zio.*
import zio.stm.*
import zio.http.*
import zio.http.model.{Method, Status}
import zio.http.middleware.HttpMiddleware

import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit


object MyServer extends ZIOAppDefault:

  val handler: ZIO[ScopedRef[Int], Nothing, Response] =
      for
        ref <- ZIO.service[ScopedRef[Int]]
        current <- ref.get
        count = current + 1
        _ <- ref.set(ZIO.succeed(count))
      yield
        Response.text(s"Count = $count")

  val app = Http.collectZIO[Request] {
    case Method.GET -> Path.root => handler
  }

  val sharedStateLayer = ZLayer.scoped {
    ScopedRef.make(0)
  }

  def run =
    Server.serve(app).provide(Server.default ++ sharedStateLayer)
