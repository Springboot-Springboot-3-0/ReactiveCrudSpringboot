# Input Validation & Reactive Error Handling

> This branch builds on the base CRUD app (`master`) and focuses on **one topic**:
> rejecting bad input early and turning failures into clean, predictable HTTP errors.
>
> Read it like a conversation: each part asks a question, answers with a little
> theory, then shows a small piece of code from this project you can run yourself.

---

### Why validate at all — can't the service just check the data?

It could, but then every method would be cluttered with `if (name == null) ...` and the
rules would be scattered and inconsistent. **Bean Validation** lets you declare the
rules **once, on the data itself**, as annotations. Spring then enforces them for you
*before* your code runs, so the controller and service only ever see valid input.

The rules live on the DTO fields:

```java
public class ProductRequest {

    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must be at most 100 characters")
    private String name;

    @Size(max = 255, message = "description must be at most 255 characters")
    private String description;

    @NotNull(message = "price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "price must be greater than zero")
    private BigDecimal price;
    // constructors + getters/setters ...
}
```

---

### What actually triggers the validation?

The `@Valid` annotation on the request body. It tells Spring: *"before calling this
method, check the incoming object against its rules."*

```java
@PostMapping
public Mono<ResponseEntity<ProductResponse>> create(@Valid @RequestBody ProductRequest request) { ... }
```

If the body breaks a rule, the method is **never entered**. Spring raises a
`WebExchangeBindException` (it holds the list of field errors), and the flow jumps
straight to error handling — described below.

---

### What about a bad value in the URL, like a negative id?

That is a different kind of validation: the argument is a single method parameter, not
an object with annotated fields. For that you put `@Validated` on the controller class
and the constraint directly on the parameter:

```java
@RestController
@RequestMapping("/api/products")
@Validated                                   // enables method-parameter validation
public class ProductController {

    @GetMapping("/{id}")
    public Mono<ProductResponse> findById(
            @PathVariable @Positive(message = "id must be greater than zero") Long id) { ... }
}
```

Now `GET /api/products/-5` fails validation before the service runs. This time the
failure is a `ConstraintViolationException` (a different type than the body case), which
is why the error handler needs to know about **both**.

---

### So how does a failure become a clean HTTP response?

This is the heart of the topic. In WebFlux you rarely `try/catch`. Instead, a failure
becomes an **error signal** travelling down the reactive stream, and one central class —
annotated `@RestControllerAdvice` — catches it and decides the HTTP response. Each
`@ExceptionHandler` maps one exception type to one response:

```java
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)      // bad @RequestBody
    ProblemDetail handleValidation(WebExchangeBindException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
        problem.setTitle("Validation failed");
        problem.setProperty("errors", ex.getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()).toList());
        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)  // bad @PathVariable/@RequestParam
    ProblemDetail handleConstraintViolation(ConstraintViolationException ex) { ... }

    @ExceptionHandler(ProductNotFoundException.class)      // business rule: missing product
    ProblemDetail handleNotFound(ProductNotFoundException ex) { ... 404 ... }
}
```

The return type `ProblemDetail` is the built-in Spring type for **RFC 7807** — the
standard JSON shape for API errors (`type`, `title`, `status`, `detail`, plus extras).

---

### That `Mono.error(...)` in the service — is it really caught by the advice?

Yes, and it is worth being precise because it surprises people.

The "not found" case is not a thrown exception; the service **returns an error signal**:

```java
public Mono<ProductResponse> findById(Long id) {
    return productRepository.findById(id)
            .switchIfEmpty(Mono.error(new ProductNotFoundException(id)))  // error signal
            .map(ProductResponse::from);
}
```

The controller returns that `Mono` unchanged. Remember **Spring is the subscriber** — it
subscribes to write the response. When it receives `onError(ProductNotFoundException)`
instead of a value, it routes that error through the **same** `@ExceptionHandler`
machinery as a thrown exception, finds `handleNotFound`, and returns a `404`.

```
service emits Mono.error(ProductNotFoundException)
   → controller returns the Mono unchanged
   → Spring subscribes, receives onError(...)
   → @ExceptionHandler(ProductNotFoundException) runs → ProblemDetail(404)
```

Two rules that follow:

- **Signal errors inside the stream** (`Mono.error(...)`, or `throw` inside a
  `map`/`flatMap`). If you swallowed it with `onErrorReturn`, the advice would never see it.
- **One place owns the HTTP shape.** Controllers/services never build error bodies; the
  advice does. This keeps every endpoint's error format identical.

---

### Where are the URLs defined? Why does `@PostMapping` have no path?

This project uses **traditional annotation-based routing**. A request's full path is the
class-level base in `@RequestMapping("/api/products")` plus the method-level path:

```
@GetMapping             -> GET    /api/products
@PostMapping            -> POST   /api/products        (no extra path: acts on the collection)
@GetMapping("/{id}")    -> GET    /api/products/{id}
@PutMapping("/{id}")    -> PUT    /api/products/{id}
@DeleteMapping("/{id}") -> DELETE /api/products/{id}
```

`@PostMapping` needs no path because "create" targets the collection itself; only
single-item operations add `"/{id}"`.

---

## Try it yourself

```bash
./gradlew bootRun     # http://localhost:8080
```

**Invalid body** (blank name, missing price) → `400` with a list of what went wrong:

```bash
curl -i -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" -d '{"name":"","description":"x"}'
# HTTP/1.1 400 Bad Request
# {"title":"Validation failed","status":400,"detail":"Request validation failed",
#  "errors":["name: name is required","price: price is required"]}
```

**Invalid path variable** (negative id) → `400`:

```bash
curl -i http://localhost:8080/api/products/-5
# HTTP/1.1 400 Bad Request  ("id must be greater than zero")
```

**Missing product** → `404`:

```bash
curl -i http://localhost:8080/api/products/999
# HTTP/1.1 404 Not Found   {"title":"Resource not found", ...}
```

Run `./gradlew test` to see this proven: `ProductControllerIntegrationTest` checks the
`400`/`404` responses, and `ApiExceptionHandlerTest` unit-tests each handler in isolation.
