import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.Tracer
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors

import scala.concurrent.{ExecutionContext, Future}
import TextMapHttpRequest.given
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.http.scaladsl.server.Directives.*

import scala.io.StdIn
import scala.util.{Success, Failure}

@main def server(): Unit =
  given actorSystem: ActorSystem[Any] = ActorSystem(Behaviors.empty, "my-system")
  given ec: ExecutionContext = actorSystem.executionContext
  val openTelemetry = GlobalOpenTelemetry.get()
  given tracer: Tracer = openTelemetry.getTracerProvider.get("open-telemetry")

  val propagator = W3CTraceContextPropagator.getInstance()

  def callJokeRoute(using ec: ExecutionContext): Route =
    SpanDirective.spanDirective("call-joke-span", propagator) {
      path("call-joke") {
        get {
          val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://localhost:9090/joke"))

          onComplete(responseFuture) {
            case Success(_) => complete("ok")
            case Failure(exception) => complete(StatusCodes.InternalServerError, "exception.")
          }
        }
      }
    }


  val bindingFuture = Http().newServerAt("localhost", 8080).bind(callJokeRoute)
  println(s"Server now online. Please navigate to http://localhost:8080/call-joke\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => actorSystem.terminate()) // and shutdown when done