import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.{TextMapGetter, TextMapPropagator, TextMapSetter}
import org.apache.pekko.http.scaladsl.model.HttpRequest
import org.apache.pekko.http.scaladsl.model.headers.RawHeader
import org.apache.pekko.http.scaladsl.server.Directive

import scala.jdk.CollectionConverters.*
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object TextMapHttpRequest {
  given requestTextMapGetter: TextMapGetter[HttpRequest] = new TextMapGetter[HttpRequest]:
    override def keys(carrier: HttpRequest): java.lang.Iterable[String] = carrier.headers.map(h => h.name).asJava

    override def get(carrier: HttpRequest, key: String): String =
      val headers = carrier.headers.filter(h => h.name == key)
      if (headers.isEmpty) null else headers.head.value

  given requestTextMapSetter: TextMapSetter[HttpRequest] = (carrier: HttpRequest, key: String, value: String) => carrier.withHeaders(RawHeader(key, value))
  
}

object SpanDirective {

  def spanDirective(spanName: String,
                    propagator: TextMapPropagator)
                   (using tracer: Tracer,
                    ec: ExecutionContext,
                    requestTextMapGetter: TextMapGetter[HttpRequest],
                    requestTextMapSetter: TextMapSetter[HttpRequest]): Directive[Unit] = Directive[Unit]:
    inner =>
      ctx =>
        val spanContext: Context = propagator.extract(Context.current(), ctx.request, requestTextMapGetter)
        val span = tracer.spanBuilder(spanName).setParent(spanContext).startSpan()
        inner(())(ctx).andThen {
          case Failure(exception) => span.addEvent(s"$spanName failed").recordException(exception).end()
          case Success(value) => span.addEvent(s"$spanName finished").end()
        }
}
