package httpclient

import java.net._
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.net.http._
import java.net.http.{HttpClient => JavaHttpClient}
import java.time.Duration
import _root_.concurrent.threadName

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

import HttpClient.logRequestResponse

final class HttpClient(baseUrl: String) {
  private val client = JavaHttpClient
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
    logRequestResponse(request, "request")
    def responseFuture  = client.sendAsync(request, BodyHandlers.ofString())
    val responsePromise = Promise[HttpResponse[String]]()
    responseFuture.whenComplete { (response, throwable) =>
      logRequestResponse(request, "response")
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

  private def logRequestResponse(
      request: HttpRequest,
      label: String
  ): Unit = {
    val pathSuffix = request.uri().getPath().split('/').last
    println(s"[${threadName()}] $label ${request.method()} $pathSuffix")
  }
}
