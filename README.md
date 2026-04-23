# Smart Campus — Sensor & Room Management API

**Module:** 5COSC022W — Client-Server Architectures  
**Student:** Thusiru Kodithuwakku  
**Technology:** JAX-RS (Jersey 2.32) with in-memory data structures

---

## 1. API Design Overview

The Smart Campus API is a RESTful web service built using JAX-RS (Jersey) that manages university rooms and their deployed sensors. The API is structured around a clear resource hierarchy. The root endpoint at `/api/v1` serves as a Discovery resource that provides API metadata and navigation links. Room management is handled at `/api/v1/rooms` for the collection and `/api/v1/rooms/{roomId}` for individual rooms. Sensor operations are exposed at `/api/v1/sensors` with support for type-based query filtering, and sensor reading history is managed through a sub-resource at `/api/v1/sensors/{sensorId}/readings`.

All data is stored in-memory using thread-safe `CopyOnWriteArrayList` collections inside a static `DataStore` class. No database technology is used. The three core models — `Room`, `Sensor`, and `SensorReading` — are POJOs implementing a shared `BaseModel` interface, managed through a reusable `GenericDAO<T>` class. Custom exception mappers handle domain-specific error scenarios (409 Conflict, 422 Unprocessable Entity, 403 Forbidden), a global catch-all mapper handles unexpected 500 errors, and a `LoggingFilter` provides API observability by logging every request and response.

---

## 2. How to Build & Run

The project requires Java 8 or higher, Apache Maven 3.6+, and Apache Tomcat 9 (or any Servlet 4.0 compatible container).

First, clone the repository and navigate into the project directory:

```
git clone https://github.com/<your-username>/smart-campus.git
cd smart-campus
```

Then build the project using Maven:

```
mvn clean package
```

This compiles the source code and produces `target/smart-campus-1.0-SNAPSHOT.war`. Copy this WAR file into Tomcat's `webapps/` directory and start the server. To verify the deployment, run:

```
curl http://localhost:8080/smart-campus/api/v1
```

A successful response will return a JSON object containing API version information, admin contact details, and links to the rooms and sensors collections.

---

## 3. Sample cURL Commands

**3.1 — Discovery Endpoint**

```
curl -X GET http://localhost:8080/smart-campus/api/v1
```

**3.2 — Get All Rooms**

```
curl -X GET http://localhost:8080/smart-campus/api/v1/rooms
```

**3.3 — Create a New Room**

```
curl -X POST http://localhost:8080/smart-campus/api/v1/rooms -H "Content-Type: application/json" -d "{\"id\": \"CAFE-01\", \"name\": \"Campus Cafe\", \"capacity\": 80}"
```

**3.4 — Create a Sensor (Linked to Existing Room)**

```
curl -X POST http://localhost:8080/smart-campus/api/v1/sensors -H "Content-Type: application/json" -d "{\"type\": \"Temperature\", \"status\": \"ACTIVE\", \"currentValue\": 21.0, \"roomId\": \"LIB-301\"}"
```

**3.5 — Get Sensors Filtered by Type**

```
curl -X GET "http://localhost:8080/smart-campus/api/v1/sensors?type=CO2"
```

**3.6 — Post a Sensor Reading (Sub-Resource)**

```
curl -X POST http://localhost:8080/smart-campus/api/v1/sensors/SENS-TEMP-001/readings -H "Content-Type: application/json" -d "{\"timestamp\": 1714000000000, \"value\": 23.7}"
```

**3.7 — Delete a Room (Blocked — Has Sensors)**

```
curl -X DELETE http://localhost:8080/smart-campus/api/v1/rooms/LIB-301
```

---

## 4. Report — Answers to Coursework Questions

### Part 1.1 — Default Lifecycle of a JAX-RS Resource Class

By default, JAX-RS resource classes follow a **per-request lifecycle**, meaning the runtime creates a new instance of the resource class for every incoming HTTP request and discards it after the response is sent. The resource class is not treated as a singleton.

This has a direct impact on data management. Since each request gets its own resource instance, instance-level fields are not shared between requests. If data were stored as instance variables inside resource classes like `SensorRoom` or `SensorResource`, it would be lost after every request. To address this, the `DataStore` class uses `static final` fields holding `GenericDAO` objects backed by `CopyOnWriteArrayList`, ensuring data persists across all requests for the lifetime of the application. The `CopyOnWriteArrayList` is a thread-safe variant of `ArrayList` from `java.util.concurrent` that prevents race conditions and `ConcurrentModificationException` when multiple concurrent requests read and modify the data simultaneously.

---

### Part 1.2 — Hypermedia and HATEOAS

HATEOAS (Hypermedia As The Engine Of Application State) is a REST constraint where the server embeds navigational links directly within API responses. Rather than requiring client developers to hardcode every possible URL, the API itself tells the client what actions are available and where to navigate next. The `Discovery` resource at `GET /api/v1` demonstrates this principle by returning a map of available resource collections (rooms and sensors) along with their URIs.

This approach benefits client developers in several important ways. Clients can discover and navigate the entire API by following links from the root endpoint, without needing to memorise URL patterns or consult external documentation. If server-side URL structures change in the future, clients that follow links dynamically will continue working without code modifications, whereas clients with hardcoded URLs would break. Additionally, the response itself serves as living documentation that is always in sync with the actual API, unlike static documentation that can become outdated over time.

---

### Part 2.1 — Returning Full Objects vs. IDs Only

The `SensorRoom.getAllRooms()` method returns full room objects including all fields (id, name, capacity, sensorIds). This approach reduces the total number of HTTP round-trips because the client receives all room data in a single request. Without this, clients would need to make follow-up `GET /rooms/{id}` requests for each room individually, resulting in an "N+1 problem" — one request for the list plus N additional requests for details. This dramatically increases total latency and client-side processing complexity.

The trade-off is higher bandwidth consumption per response. If the client only needs room names for a dropdown menu, sending full objects with sensor ID lists is wasteful data transfer. However, for a campus system with a manageable number of rooms, returning full objects is the more efficient and pragmatic choice. An IDs-only approach would be better suited for very large datasets where lazy-loading of details is preferred.

---

### Part 2.2 — Idempotency of DELETE

The DELETE operation in this implementation is **not strictly idempotent** in terms of HTTP response. The first call successfully deletes the room and returns **204 No Content**. When the same request is sent a second time, `DataStore.roomDAO.getById()` returns `null` because the room no longer exists, and a `DataNotFoundException` is thrown, resulting in **404 Not Found**.

Since the same request produces different response codes (204 then 404), the operation is not idempotent in terms of response. However, it is idempotent in terms of **server-side effect** — after the first successful deletion, the resource is gone and no further state change occurs on any subsequent call. The server's state remains identical regardless of how many times the DELETE is repeated. This approach is common in production APIs because a 404 provides useful feedback to the client, indicating that the resource does not exist, which helps catch bugs where stale data is being referenced.

---

### Part 3.1 — Consequences of `@Consumes(APPLICATION_JSON)`

The `@Consumes(MediaType.APPLICATION_JSON)` annotation on the `createSensor()` POST method in `SensorResource` tells the JAX-RS runtime that this method only accepts request bodies with the `Content-Type: application/json` header.

If a client sends a request with a different content type, such as `text/plain` or `application/xml`, the JAX-RS runtime will not invoke the method at all. Instead, Jersey performs content negotiation by matching the request's `Content-Type` header against the `@Consumes` annotations of all candidate methods. When no method matches the incoming media type, the framework short-circuits the request and automatically returns **HTTP 415 Unsupported Media Type**. This happens at the framework level before any business logic executes, providing declarative enforcement of the contract without the need for manual header inspection or conditional logic.

---

### Part 3.2 — `@QueryParam` vs. Path-Based Filtering

The `getSensors()` method in `SensorResource` uses `@QueryParam("type")` to support optional filtering (e.g., `?type=CO2`). This is superior to a path-based alternative like `/sensors/type/CO2` for several reasons.

Query parameters are inherently optional — the same endpoint `/sensors` serves both unfiltered and filtered requests without requiring separate route definitions. With path-based filtering, separate `@Path` annotations and methods would be needed for each filter. Furthermore, query parameters are composable — multiple filters can be easily combined (e.g., `?type=CO2&status=ACTIVE`), whereas path-based designs create rigid, deeply nested URLs and require a combinatorial explosion of route definitions for every possible filter combination. From a RESTful semantics perspective, the URL path should identify a resource or collection, while filters and sorting are modifiers on that collection — query parameters correctly express this distinction. Finally, adding a new filter requires only an additional `@QueryParam` parameter rather than restructuring URLs with new `@Path` annotations.

---

### Part 4.1 — Benefits of the Sub-Resource Locator Pattern

The `SensorResource` class contains a sub-resource locator method at `@Path("/{sensorId}/readings")` that returns a new `SensorReadingResource` instance. This method has a `@Path` annotation but no HTTP method annotation (`@GET`, `@POST`), which is the defining characteristic of a sub-resource locator — it does not handle the request itself but delegates to a separate class.

This pattern provides significant architectural benefits. It enforces separation of concerns by assigning each resource class to a single domain concept: `SensorResource` manages sensors while `SensorReadingResource` handles reading history. Without this pattern, all paths (`/sensors`, `/sensors/{id}`, `/sensors/{id}/readings`) would be defined in one massive class that grows unmanageable as the API expands with new nested resources. The locator method also encapsulates context by capturing the `sensorId` from the path and passing it to the sub-resource constructor, allowing `SensorReadingResource` to operate within that sensor's scope without re-parsing the URL. In a larger development team, this separation allows different developers to work on separate resource classes independently without merge conflicts.

---

### Part 5.2 — Why HTTP 422 Over 404

HTTP 404 means "the URL you requested does not exist." When a client sends `POST /api/v1/sensors` with a `roomId` that does not exist in the system, the URL `/api/v1/sensors` itself is perfectly valid and correctly routed. Returning 404 would mislead the client into thinking the sensors endpoint is missing.

HTTP 422 (Unprocessable Entity) means "the request is syntactically valid, but the content is semantically incorrect and cannot be processed." This precisely describes the scenario handled by `LinkedResourceNotFoundExceptionMapper`: the JSON payload is well-formed and the `Content-Type` header is correct, but the `roomId` value references a room that does not exist — a business logic violation, not a routing problem. Using 422 guides the client developer toward inspecting their request body values rather than their URL construction, providing a much clearer signal about what went wrong and where to look for the fix.

---

### Part 5.4 — Security Risks of Exposing Stack Traces

The `GlobalExceptionMapper` catches all unexpected `Throwable` exceptions and returns a generic, sanitised error message, preventing internal implementation details from being leaked to external consumers.

Without this safety net, raw Java stack traces could expose critical information to attackers. Full class names such as `com.example.smart.campus.resources.SensorRoom.deleteRoom()` reveal the application's internal architecture, package naming conventions, and class hierarchy. Framework classes in the trace (e.g., `org.glassfish.jersey.servlet.WebComponent`) disclose the exact technology stack and version, allowing attackers to search for known CVEs (Common Vulnerabilities and Exposures) specific to that version. File paths and line numbers can expose the operating system, deployment directory structure, and even developer usernames. Exception messages and method call chains can reveal how validation logic works, what checks are performed, and where security boundaries exist, helping attackers craft targeted payloads to bypass those checks.

---

### Part 5.5 — Advantages of JAX-RS Filters for Logging

The `LoggingFilter` class implements both `ContainerRequestFilter` and `ContainerResponseFilter` in a single class, using `java.util.logging.Logger` to log the HTTP method and URI for every incoming request, and the HTTP status code for every outgoing response.

This approach is far superior to manually inserting `Logger.info()` statements inside every resource method. The logging logic is written once and applied automatically by the JAX-RS runtime to every request, eliminating code duplication across all 9+ resource methods in this API. Manual logging depends on developers remembering to add it to every new method — if someone adds an endpoint and forgets the logging statements, that endpoint becomes invisible in the logs. Filters guarantee complete coverage. Additionally, logging is a cross-cutting infrastructure concern that has nothing to do with business logic. Mixing the two makes methods harder to read and maintain. With a centralised filter, changing the log format, adding timestamps, or switching to a different logging framework requires editing a single file instead of modifying every resource method across the entire codebase.
