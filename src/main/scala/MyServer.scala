import zio.*
import zio.stm.*
import zio.http.*
import zio.http.model.{Method, Status}
import zio.http.middleware.HttpMiddleware

import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit


object MyServer extends ZIOAppDefault:

  def app(ref: Ref[Int]) = Http.collectZIO[Request] {
    case Method.GET -> Path.root =>
      ref.updateAndGet(_ + 1).map { count => Response.text(s"Count = $count") }
  }

  val sharedState = Ref.make(0)

  def run =
    for
      ref <- sharedState
      server <- Server.serve(app(ref)).provide(Server.default)
    yield
      server
