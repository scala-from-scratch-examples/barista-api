package httpserver

import scala.concurrent.Future

final case class Request(method: String, path: String, body: String)
final case class Response(status: Int, body: String)

object Response {
  def ok(body: String): Response               = Response(200, body)
  def notFound(body: String): Response         = Response(404, body)
  def methodNotAllowed(body: String): Response = Response(405, body)
  def badRequest(body: String): Response       = Response(400, body)
}

final class Router(routes: PartialFunction[Request, Future[Response]])
    extends (Request => Future[Response]) {
  override def apply(request: Request): Future[Response] =
    if (routes.isDefinedAt(request)) routes(request)
    else
      Future.successful(
        Response.notFound(s"No matching route for ${request.path}")
      )
}

object Router {
  def of(routes: PartialFunction[Request, Future[Response]]): Router =
    new Router(routes)
}
