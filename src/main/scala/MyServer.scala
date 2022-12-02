import zio.*
import zio.stm.*
import zio.http.*
import zio.http.model.{Method, Status}
import zio.http.middleware.HttpMiddleware

import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit


object MyServer extends ZIOAppDefault:

  def handler(ref: Ref.Synchronized[Int]): ZIO[Any, Nothing, Response] =
      for
        count <- ref.updateAndGet(_ + 1)
      yield
        Response.text(s"Count = $count")

  def app(ref: Ref.Synchronized[Int]) = Http.collectZIO[Request] {
    case Method.GET -> Path.root => handler(ref)
  }

  val sharedStateLayer = ZLayer.scoped {
    Ref.Synchronized.make(0)
  }

  val middleware = Middleware.collectZIO

  def run =
    for
      ref <- Ref.Synchronized.make(0)
      server <- Server.serve(app(ref)).provide(Server.default)
    yield
      server
