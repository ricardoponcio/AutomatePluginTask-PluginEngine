# PluginEngine

## An Automate Plugin Task module

PluginEngine is the backend service for managing, executing, and monitoring plugins built with the [PluginSdk](../PluginSdk/README.md). It enables dynamic plugin loading, synchronous/asynchronous execution, progress tracking, and plugin file storage using S3.

---

## Features

- Upload and register plugin JARs dynamically
- Retrieve plugin input and base parameter definitions
- Execute plugins synchronously or asynchronously (with progress reporting)
- Store plugin files in S3-compatible storage
- Manage S3 configurations (create, list, delete)
- RESTful API for all operations
- Exception handling and logging

---

## Getting Started

### Prerequisites

- Java 17+
- Maven
- S3-compatible storage (AWS S3, MinIO, etc.)

### Build and Run

```sh
mvn clean package
java -jar target/PluginEngine-0.0.1-SNAPSHOT.jar
```

---

## API Overview

### S3 Configuration

- `POST /api/v1/s3/config` — Create S3 configuration
- `GET /api/v1/s3/config` — List S3 configurations
- `DELETE /api/v1/s3/config/{id}` — Delete S3 configuration

### Plugin Management

- `POST /api/v1/plugin/v1/persist` — Upload a plugin JAR file  
  _Returns a UUID for the plugin record._
- `GET /api/v1/plugin/v1/parameters/retrieve/{uuid}` — Get plugin input parameter definitions
- `GET /api/v1/plugin/v1/base-parameters/retrieve/{uuid}` — Get plugin base parameter definitions
- `POST /api/v1/plugin/v1/base-parameters/{uuid}/set` — Set base parameters for a plugin

### Plugin Execution

- `POST /api/v1/plugin/v1/load/{uuid}` — Execute plugin synchronously
- `POST /api/v1/plugin/v1/loadAsync/{uuid}` — Execute plugin asynchronously (with progress reporting)

---

## Usage Example

1. **Upload a plugin JAR:**
   ```sh
   curl -F "file=@SampleHelloPlugin-0.0.1-SNAPSHOT.jar" http://localhost:8080/api/v1/plugin/v1/persist
   ```
   _Response will include the plugin UUID._

2. **Retrieve parameters:**
   ```sh
   curl http://localhost:8080/api/v1/plugin/v1/parameters/retrieve/{uuid}
   ```

3. **Execute plugin:**
   ```sh
   curl -X POST -H "Content-Type: application/json" \
     -d '{"inputParameters":[...],"baseParameters":[...]}' \
     http://localhost:8080/api/v1/plugin/v1/load/{uuid}
   ```

---

## S3 Storage Notes

- S3 configuration is required before uploading plugins.
- Only create, list, and delete are allowed for S3 configs (no update for security reasons).

---

## Development Notes

- The engine uses a custom class loader to load plugin JARs and extract the main class from the manifest.
- Plugins must implement the `AbstractPluginTask` from PluginSdk.
- Exception handling is centralized in `GlobalExceptionHandler`.

---

## References

- [PluginSdk](https://github.com/ricardoponcio/AutomatePluginTask-PluginSdk) — How to build compatible plugins
- [SampleHelloPlugin](https://github.com/ricardoponcio/AutomatePluginTask-SampleHelloPlugin) — Example plugin implementation
- [Main Repository](https://github.com/ricardoponcio/AutomatePluginTask)

---

## License

MIT

---

## Contact

Any doubts? Send a message:  
E-mail: **ricardo@poncio.dev**  
Telegram: **@rponcio**
