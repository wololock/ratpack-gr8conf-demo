import app.FakeRemoteProductService
import app.ProductHandler
import app.ProductService
import app.RecommendationsHandler
import io.github.resilience4j.ratpack.Resilience4jModule

import static ratpack.groovy.Groovy.ratpack

ratpack {
  serverConfig {
    threads(1)
    development(false)
  }

  bindings {
    bind ProductHandler
    bind RecommendationsHandler
    bind ProductService, FakeRemoteProductService
    module Resilience4jModule
  }

  handlers {
    get "products/:id", ProductHandler

    get "recommendations", RecommendationsHandler
  }
}
