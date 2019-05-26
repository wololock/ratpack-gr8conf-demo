package app

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.ratpack.circuitbreaker.CircuitBreakerTransformer
import ratpack.exec.Promise
import ratpack.exec.util.ParallelBatch
import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.http.client.HttpClient

import javax.inject.Inject
import java.time.Duration

import static ratpack.jackson.Jackson.json

class RecommendationsHandler implements Handler {

  private final List<String> IDS = [
      'PROD-003',
      'PROD-001',
      'PROD-004',
      'PROD-005',
      'PROD-002'
  ]

  private final HttpClient http
  private final ObjectMapper mapper
  private final CircuitBreakerRegistry circuitBreakerRegistry

  @Inject
  RecommendationsHandler(HttpClient http, ObjectMapper mapper, CircuitBreakerRegistry circuitBreakerRegistry) {
    this.http = http
    this.mapper = mapper
    this.circuitBreakerRegistry = circuitBreakerRegistry
  }

  @Override
  void handle(Context ctx) throws Exception {
    def promises = IDS.collect { id -> prepareHttpRequest(id) }

    ParallelBatch.of(promises)
      .publisher()
      .toList()
      .then { products ->
        ctx.render(json(products))
      }
  }

  private Promise<Product> prepareHttpRequest(String id) {
    return http.get("http://localhost:5050/products/${id}".toURI(), { request ->
      request.readTimeout(Duration.ofMillis(500))
    }).mapIf(
        { response -> response.status.is2xx() },
        { response -> mapper.readValue(response.body.inputStream, Product) },
        { _ -> null }
    ).transform(
        CircuitBreakerTransformer.of(circuitBreakerRegistry.circuitBreaker(id))
    ).onError { e ->
      println e.message
    }
  }
}

