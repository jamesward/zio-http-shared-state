import zio.*
import zio.stm.*
import zio.http.*
import zio.http.model.{Method, Status}
import zio.http.middleware.HttpMiddleware

import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit


object MyServer extends ZIOAppDefault:

  val handler: ZIO[Ref.Synchronized[Int], Nothing, Response] =
      for
        ref <- ZIO.service[Ref.Synchronized[Int]]
        count <- ref.updateAndGet(_ + 1)
      yield
        Response.text(s"Count = $count")

  val app = Http.collectZIO[Request] {
    case Method.GET -> Path.root => handler
  }

  val sharedStateLayer = ZLayer.scoped {
    Ref.Synchronized.make(0)
  }

  def run =
    Server.serve(app).provide(Server.default ++ sharedStateLayer)
