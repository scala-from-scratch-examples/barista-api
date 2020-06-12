package httpclient

import java.net._
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.net.http._
import java.time.Duration

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

final class HttpClient(baseUrl: String) {
  private val client = java.net.http.HttpClient
    .newBuilder()
    .connectTimeout(Duration.ofSeconds(2))
    .build()

  def post(path: String, body: String): Future[HttpResponse[String]] =
    sendRequest(postRequest(URI.create(s"$baseUrl$path"), body))

  def get(path: String): Future[HttpResponse[String]] =
    sendRequest(getRequest(URI.create(s"$baseUrl$path")))

  private def getRequest(uri: URI): HttpRequest =
    HttpRequest
      .newBuilder()
      .uri(uri)
      .timeout(Duration.ofSeconds(10))
      .header("Accept", "text/plain")
      .GET()
      .build()

  private def postRequest(uri: URI, body: String): HttpRequest =
    HttpRequest
      .newBuilder()
      .uri(uri)
      .timeout(Duration.ofSeconds(10))
      .header("Accept", "text/plain")
      .POST(BodyPublishers.ofString(body))
      .build()

  private def sendRequest(
      request: HttpRequest
  ): Future[HttpResponse[String]] = {
    def responseFuture  = client.sendAsync(request, BodyHandlers.ofString())
    val responsePromise = Promise[HttpResponse[String]]()
    responseFuture.whenComplete { (response, throwable) =>
      if (throwable != null)
        responsePromise.complete(Failure(throwable))
      else responsePromise.complete(Success(response))
    }
    responsePromise.future
  }
}
object HttpClient {
  def ifOk[A](
      bodyHandler: String => A
  )(response: HttpResponse[String]): Future[A] =
    response.statusCode() match {
      case 200 => Future.successful(bodyHandler(response.body()))
      case status =>
        Future.failed(new Exception(s"Unexpected status code $status"))
    }
  def encode(urlPart: String): String =
    java.net.URLEncoder.encode(urlPart, "UTF-8")
}
