# Reactive CRUD Basics

> This is the **base branch** (`master`) that every other topic branch builds on.
> It is the smallest complete **non-blocking CRUD REST API** using Spring WebFlux and R2DBC.
>
> Read it top to bottom like a conversation: each part asks a question, answers it
> with a little theory, then shows a small piece of code from this project that you
> can run yourself.

---

### What problem is "reactive" actually solving?

Think about a normal web app. A request comes in, the code asks the database for a
row, and then it **waits**. While it waits, the thread handling that request is stuck
doing nothing — it cannot help anyone else. If 200 requests arrive and each waits on
a slow database, you need ~200 threads sitting idle. Threads are expensive, so the
server struggles.

Reactive programming flips this around. Instead of *waiting* for the database, your
code says: *"here is what to do **when** the data arrives"* and immediately hands the
thread back so it can serve someone else. A handful of threads can now juggle
thousands of requests.

So "reactive" is really about **not blocking a thread while waiting**.

---

### If methods don't return the data, what do they return?

They return a **promise of data that will arrive later**. In Project Reactor (the
library WebFlux uses) there are two such types:

| Type | Meaning | Real example here |
|------|---------|-------------------|
| `Mono<T>` | at most **one** item, later | one product, or "not found" |
| `Flux<T>` | a **stream** of 0..N items, later | the list of all products |

A good mental model: `Mono`/`Flux` is a **recipe**, not the finished meal. It
describes the steps ("get the product, then convert it") but nothing happens until
someone decides to cook it.

```java
// This does NOT hit the database yet — it just describes the work.
Mono<Product> product = productRepository.findById(1L);
```

---

### If nothing runs until someone "cooks the recipe", who does that?

Someone has to **subscribe** to the publisher to make it run. The important part for
a web app: **you almost never subscribe yourself.** Spring WebFlux subscribes for
you when it is ready to write the HTTP response.

That is why controller methods simply **return** the `Mono`/`Flux` and never call
`.block()`:

```java
@GetMapping("/{id}")
public Mono<ProductResponse> findById(@PathVariable Long id) {
    return productService.findById(id);   // just hand back the recipe
}
```

Rule of thumb: **build and return publishers; don't wait on them.** Calling
`.block()` would put the waiting (and the wasted thread) right back — the very thing
we were avoiding.

---

### Where are the URLs defined? Why does `@PostMapping` have no path?

This project uses **traditional annotation-based routing**: the URL for each endpoint
is declared right on the method with an annotation. A request's full path is built
from **two parts**:

1. the class-level base path in `@RequestMapping`, and
2. the method-level path in `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping`.

```java
@RestController
@RequestMapping("/api/products")     // (1) base path for every method in this class
public class ProductController {

    @GetMapping                       // GET    /api/products         (base + nothing)
    @PostMapping                      // POST   /api/products         (base + nothing)
    @GetMapping("/{id}")              // GET    /api/products/{id}    (base + "/{id}")
    @PutMapping("/{id}")              // PUT    /api/products/{id}
    @DeleteMapping("/{id}")           // DELETE /api/products/{id}
}
```

So `@PostMapping` **does** have a route — it is `POST /api/products`. It needs no extra
path because "create a product" acts on the **collection** (`/api/products`), not on one
specific item. Only the operations that target a single existing item add `"/{id}"`.
The HTTP method in the annotation name (`GET`/`POST`/`PUT`/`DELETE`) is what
distinguishes `GET /api/products` from `POST /api/products`.

Then what about this line inside `create(...)`?

```java
return ... ResponseEntity.created(URI.create("/api/products/" + response.id())) ...
```

That is **not** routing — it is the response. After a successful `POST`, REST
convention is to return `201 Created` with a `Location` header pointing at the URL of
the newly created resource. That URL contains the new `id`, which only exists *after*
the row is saved, so it is assembled in code rather than declared in an annotation.

> Note: WebFlux also supports a *functional* style (`RouterFunction`/`RouterFunctions`)
> where routes are defined in code instead of annotations. Every branch in this repo
> uses the annotation style shown above, because it is the most common and readable
> starting point.

---

### How do I read and write data without blocking?

You let Spring Data R2DBC do it. You declare an **interface**, and Spring generates a
non-blocking implementation where every method already returns `Mono`/`Flux`:

```java
public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {
}
// findAll()      -> Flux<Product>
// findById(id)   -> Mono<Product>
// save(product)  -> Mono<Product>
// delete(product)-> Mono<Void>
```

You wrote no SQL and no query code, yet you have full reactive CRUD.

---

### How do I add logic without falling back to blocking code?

You **compose** the recipe using operators like `map` (transform the item) and
`switchIfEmpty` (provide a fallback when nothing was emitted). This replaces the
usual imperative `if (x == null) { ... }`.

Here is the whole "find a product, or fail with a 404" rule expressed as one chain:

```java
public Mono<ProductResponse> findById(Long id) {
    return productRepository.findById(id)                       // Mono<Product> (maybe empty)
            .switchIfEmpty(Mono.error(new ProductNotFoundException(id))) // empty -> error
            .map(ProductResponse::from);                        // Product -> ProductResponse
}
```

Read it like a sentence: *find the product; if there isn't one, raise not-found;
otherwise convert it to the response shape.* No thread ever blocks.

---

### That `Mono.error(...)` — can `@RestControllerAdvice` actually catch it?

Yes, and this trips people up, so it is worth being precise.

In a normal (blocking) app you `throw` an exception and Spring catches it. Here we did
**not** throw — we returned `Mono.error(new ProductNotFoundException(id))`. That is not
a thrown exception; it is an **error signal** flowing through the reactive stream. The
key insight:

> An error signal that reaches the end of the `Mono`/`Flux` a controller returns is
> handed to the **exact same** `@ExceptionHandler` machinery as a thrown exception.

Remember who subscribes — **Spring does** (see the "recipe" question above). When it
subscribes and receives an `onError(ProductNotFoundException)` instead of a value, it
looks for a matching `@ExceptionHandler`, finds the one in our
`@RestControllerAdvice`, and uses its result as the response.

So this handler catches the not-found signal from `switchIfEmpty`:

```java
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    ProblemDetail handleNotFound(ProductNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource not found");
        return problem;                       // becomes a 404 JSON body
    }
}
```

Requesting a missing product returns a clean **404** (RFC 7807 `ProblemDetail`):

```bash
curl -i http://localhost:8080/api/products/999
# HTTP/1.1 404 Not Found
# {"type":"about:blank","title":"Resource not found","status":404,
#  "detail":"Product 999 was not found"}
```

**The full path of the error:**

```
service: findById(999)
   → repository emits empty (no row)
   → switchIfEmpty replaces empty with Mono.error(ProductNotFoundException)
controller: returns that Mono unchanged
Spring: subscribes, receives onError(ProductNotFoundException)
advice: @ExceptionHandler(ProductNotFoundException) runs → ProblemDetail(404)
client: gets a 404 JSON response
```

Two rules that follow from this:

- **Signal the error inside the stream** (`Mono.error(...)`, or `throw` inside a
  `map`/`flatMap` lambda). If you instead threw *outside* the reactive chain, or
  swallowed the error with something like `onErrorReturn`, the advice would never see it.
- **One place decides the HTTP shape.** Controllers and services never build error
  responses; the advice does. This is proven here by the integration test
  `shouldReturnNotFoundForMissingProduct`, which asserts the `404`.

---

## The pieces in this branch

```
Product                  the table row (mapped to the PRODUCTS table)
ProductRepository        reactive data access (ReactiveCrudRepository)
ProductService           business logic; composes Mono/Flux
ProductController        REST endpoints; returns Mono/Flux
ProductRequest/Response  input/output DTOs (plain Java classes / POJOs)
```

## Try it yourself

Start the app and create a product:

```bash
./gradlew bootRun     # http://localhost:8080

curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","description":"Lightweight","price":999.99}'

curl http://localhost:8080/api/products        # see it come back in the list
```

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/products` | list all products (`Flux`) |
| GET | `/api/products/{id}` | get one product (`Mono`, 404 if missing) |
| POST | `/api/products` | create (201 + `Location`) |
| PUT | `/api/products/{id}` | update |
| DELETE | `/api/products/{id}` | delete (204) |

The database is in-memory **H2** (via R2DBC), created on startup from
`src/main/resources/schema.sql` — nothing to install. Run `./gradlew test` to see
the unit and integration tests pass.

---

## Where to go next

Each other branch takes this same app and demonstrates one focused WebFlux topic:

- `input-validation-reactive-error-handling` – validating input and returning clean errors
- `reactive-data-access-spring-data-r2dbc` – custom R2DBC `@Query` methods
- `reactive-data-streaming` – streaming data with Server-Sent Events
- `webclient` – calling other services reactively with `WebClient`
- `webfilter-interception-cross-cutting-logic` – cross-cutting logic with a `WebFilter`
- `high-performance-gzip-connection-pooling-http2` – gzip, connection pooling, HTTP/2
- `end-to-end-reactive-microservices-project` – composing services end to end
