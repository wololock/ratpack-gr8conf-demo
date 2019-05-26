package app

import ratpack.exec.Blocking
import ratpack.exec.Promise

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

class FakeRemoteProductService implements ProductService {

  static final Map<String, Supplier<Product>> products = [
      'PROD-001': productWithFakeLatency(
          new Product('PROD-001', 'Learning Ratpack', 34.99), 80
      ),
      'PROD-002': productWithFakeLatency(
          new Product('PROD-002', 'Netty In Action', 39.99), 110
      ),
      'PROD-003': productWithFakeLatency(
          new Product('PROD-003', 'Micronaut In Action', 35.99), 600
      ),
      'PROD-004': productWithFakeLatency(
          new Product('PROD-004', 'Groovy In Action, 2n edt', 39.99), 1200
      ),
      'PROD-005': productWithFakeLatency(
          new Product('PROD-005', 'Code Complete', 45.99), 1500
      )
  ] as ConcurrentHashMap

  static Supplier<Product> productWithFakeLatency(Product product, int latency) {
    return {
      Thread.sleep(latency)
      return product
    } as Supplier<Product>
  }

  @Override
  Promise<Product> findProductById(String id) {
    return Blocking.get {
      products.getOrDefault(id, {} as Supplier).get()
    }
  }
}
