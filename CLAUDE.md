# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

This project has both Maven and Gradle build files. **Gradle is preferred** (the Dockerfile uses `build/libs/*.jar`).

```bash
# Build
./gradlew build
mvn clean package

# Run tests
./gradlew test
mvn test

# Run single test class
./gradlew test --tests "org.modelseeed.vault.AppTest"
mvn test -Dtest=AppTest

# Run application
./gradlew bootRun
java -jar build/libs/vault-0.0.1-SNAPSHOT.jar
```

## Architecture

**Vault** is a bioinformatics REST API for managing genome-scale metabolic models (GEMs) and biological data. It uses an embedded Neo4j instance (not a remote server) as its primary graph database, with MongoDB for protein/document data and RabbitMQ for async messaging.

### Key Architectural Point: Embedded Neo4j

The project uses **Neo4j's embedded database API** (`DatabaseManagementServiceBuilder`), not the Spring Data Neo4j / Bolt driver. The database path is hardcoded in `Neo4jConfig.java`:

```java
public static Path DEFAULT_DATABASE_PATH = Paths.get("M:/vault/graphdb");
// Linux alternative: Paths.get("graphdb")
```

**Change this path before running on Linux.** The `graphdb/` directory is gitignored.

### Layer Structure

```
Controller → Service → Repository → GraphDatabaseService (Neo4j embedded)
                                  → MongoClient (MongoDB)
```

- **`neo4j/cobra/`** — Neo4j node/relationship models for COBRA metabolic data (`Neo4jReaction`, `Neo4jMetabolite`)
- **`core/cobra/`** — Plain domain objects (`Model`, `Reaction`, `Metabolite`, `Gene`)
- **`biodb/`** — Biochemical ontology matching and mapping (`ReactionMatcher`, `ReactionMapper`)
- **`beleif/`** — Hypothesis/belief tracking system (note: intentional package name typo)
- **`rast/`** — RAST genome annotation service client

### Neo4j Labels and Relationships

Defined as enums: `LabelCOBRA`, `RelationshipCOBRA`, `OntologyBiodb`, `OntologyRelationship`. Always use these enums rather than raw strings when writing Cypher or graph operations.

### REST API

All controllers use `@CrossOrigin`. Swagger/OpenAPI docs available at `/swagger-ui.html` when running.

Main endpoints:
- `/cobra/model/{id}` — COBRA metabolic model retrieval
- `/graph/node/...` — Generic graph node CRUD
- `/protein`, `/genome`, `/contig` — Biological data endpoints

## External Service Dependencies

The application requires all three services to start:
- **Neo4j** — embedded, path configured in `Neo4jConfig.java`
- **MongoDB** — `mongodb://127.0.0.1:27017`
- **RabbitMQ** — localhost default
