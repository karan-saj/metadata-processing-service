# Metadata Processing Service

## Overview
The **Metadata Processing Service** is a Spring Boot-based application designed to ingest, 
validate, process, and propagate metadata events. Implementation of pub sub architecture insures that
the app is scalable and efficent. It supports multiple metadata formats (JSON, CSV, etc.), 
checks the rules based on each event topic and user permission level. Proceeds to process and 
apply security layers on data by masking and encryption. Based on the rules configures creates the
outbound topic with data in message based on the access level of the target.

## Features
- **Multi-format Support:** Processes JSON, CSV, and other metadata formats.
- **Schema Validation:** Ensures metadata conforms to predefined schemas.
- **Metadata Enrichment:** Adds missing fields, deduplicates, and standardizes data.
- **Data Security:** Implements masking and encryption for sensitive fields.
- **Change Data Capture (CDC):** Tracks changes and generates lineage records.
- **Lineage:** Tracks lineage (send seperate event or store lineage based on business logic)
- **Event Propagation:** Uses Kafka to send processed metadata downstream.
- **Rule-Based Processing:** Applies custom business rules to metadata.
- **Logging & Exception Handling:** Provides robust error handling and logging.
- **Data Storage:** Stores data in postgres for later reference and quick lookup 

## Tech Stack
- **Java 21**
- **Spring Boot 3.4.2**
- **Kafka** (for event propagation)
- **PostgreSQL** (for metadata persistence)
- **Lombok** (for reducing boilerplate code)
- **Jackson** (for JSON processing)

## Project Structure
```
metadataProcessingService/
├── src/main/java/com/lily/metadataProcessingService
│   ├── common/                        # Common enum and constant
│   ├── controller/                    # API Endpoints
│   ├── consumer/                      # Service to consume events for inbound topic
│   ├── dto/                           # Data Transfer Objects
│   ├── exception/                     # Custom Exceptions
│   ├── producer/                      # Kafka Producer Service
│   ├── repository/                    # Database Repository
│   ├── service/                       # Business Logic Services
│   │   ├── IngestionService.java      # Metadata pre-processing
│   │   ├── PreProcessingService.java  # Metadata pre-processing
│   │   ├── ProcessingService.java     # Core processing & CDC generation
│   ├── util/                          # Utility Classes (Encryption, Validation, etc.)
│   ├── config/                        # Configuration Files
├── src/main/resources/
│   ├── application.properties # App Configurations
│   ├── schema.sql             # Database Schema
├── pom.xml                    # Maven Dependencies
└── README.md                  # Documentation
```

## Application Flow
1. **Metadata Ingestion:** API receives metadata payloads via HTTP requests (controller or consumer)
2. **Pre-Processing:**
    - Validates source has the right access and the data type is correct
    - Fetches rule based on current source type, region, event and topic
    - Determines metadata type (JSON, CSV, etc.).
    - Validates schema and converts into a common dto for processing
    - Applies enrichment (deduplication, missing fields, masking, encryption).
3. **Core Processing:** 
    - Fetches previous metadata state from the database.
    - Determines operation type based on source and metadata
    - Generates CDC (Change Data Capture) events.
    - Generates lineage form CDC and stores/send events for same
4. **Propagation:**
    - Stores metadata in PostgreSQL for history tracking.
    - Creates message for outbound metadata based on the rule and target topic
    - Publishes processed metadata to a Kafka topic.
5. **Response:** Returns processing status to the user.

## Installation & Setup
### Prerequisites
- Java 21
- Maven 3+
- Kafka (locally or via Docker)
- PostgreSQL

### Clone the Repository
```sh
git https://github.com/karan-saj/metadata-processing.git
cd metadata-processing
```

### Configure Application Properties
Update `src/main/resources/application.properties` with:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/metadata_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.kafka.bootstrap-servers=localhost:9092
```

### Build & Run the Application
```sh
mvn clean install
mvn spring-boot:run
```

## API Endpoints
### 1. Upload Metadata
**Endpoint:** `POST /api/metadata/upload`

**Request Body:**
```json
{
  "eventType": "ingestion",
  "metadata": {
    "id": "1234",
    "name": "sample.json",
    "format": "json"
  }
}
```

### 2. Check Processing Status
**Endpoint:** `GET /api/metadata/status/{id}`

**Response:**
```json
{
  "id": "1234",
  "status": "Processed"
}
```

## Future Enhancements
- **Enhanced Logging:** Detailed logging for each step. Actuator is integrated for performance but more details should be added
- **Documentation:** Detailed documentation and swagger implementation
- **Machine Learning Integration:** LLM integration for detecting metadata type and auto parsing it into common dto
- **Enhanced Retrial Mechanisim:** Checks if the metadata shared is corrupted, allows partial processing for corrupted data
- **UI Dashboard:** Provide a web interface for monitoring processing.
- **Large File Support:** Support for large metadata files upto 1GB and allowing same in inbound and outbound request using multipart, streaming
- **Archival storage:** Linking storage with s3 to have daily archival storage for backup and sharing large files
- **Multiple Topic Support:** Supporting multiple inbound and outbound topics and deep integration with rules
- **Instance management:** Having docker/k8s setup for efficent instance management. Having circuit breaker and exponential backoff of failure scenario
