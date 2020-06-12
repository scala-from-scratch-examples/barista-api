package fitzgerald

import httpserver.Request

object ~> {
  def unapply(request: Request): Option[(Request, Request)] =
    Some((request, request))
}

object Methods {
  class MethodExtractor(methodName: String) {
    def unapply(request: Request): Boolean = request.method == methodName
  }
  val GET  = new MethodExtractor("GET")
  val POST = new MethodExtractor("POST")
}

object HttpRequest {
  def unapply(request: Request): Option[(String, String, String)] =
    Some((request.method, request.path, request.body))
}

object Path {
  def unapplySeq(request: Request): Option[Seq[String]] =
    request.path.split("/").toSeq match {
      case Seq("", tail @ _*) => Some(tail)
      case segments           => Some(segments)
    }
}

object RootPath {
  def unapply(request: Request): Boolean = request.path == "/"
}

object LongVar {
  def unapply(pathVariable: String): Option[Long] =
    pathVariable.toLongOption
}
