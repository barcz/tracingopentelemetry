import TextMapHttpRequest.given
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.server.Directives.*
import org.apache.pekko.http.scaladsl.server.{Directive, Directives, Route, RouteResult}

import scala.concurrent.ExecutionContext
import scala.io.StdIn

@main def client(): Unit =

  val propagator = W3CTraceContextPropagator.getInstance()

  given actorSystem: ActorSystem[Any] = ActorSystem(Behaviors.empty, "my-system")
  given ec: ExecutionContext = actorSystem.executionContext
  val openTelemetry = GlobalOpenTelemetry.get()
  given tracer: Tracer = openTelemetry.getTracerProvider.get("open-telemetry")

  val jokeRoute: Route =
      SpanDirective.spanDirective("joke-span", propagator) {
        path("joke") {
          get {
            complete("Joke")
          }
        }
      }






  val bindingFuture = Http().newServerAt("localhost", 9090).bind(jokeRoute)
  println(s"Server now online. Please navigate to http://localhost:9090/joke\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => actorSystem.terminate()) // and shutdown when done