# Spring PetClinic Sample Application [![Build Status](https://github.com/spring-projects/spring-petclinic/actions/workflows/maven-build.yml/badge.svg)](https://github.com/spring-projects/spring-petclinic/actions/workflows/maven-build.yml)[![Build Status](https://github.com/spring-projects/spring-petclinic/actions/workflows/gradle-build.yml/badge.svg)](https://github.com/spring-projects/spring-petclinic/actions/workflows/gradle-build.yml)

[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/spring-projects/spring-petclinic) [![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://github.com/codespaces/new?hide_repo_select=true&ref=main&repo=7517918)

## Understanding the Spring Petclinic application with a few diagrams

See the presentation here:  
[Spring Petclinic Sample Application (legacy slides)](https://speakerdeck.com/michaelisvy/spring-petclinic-sample-application?slide=20)

> **Note:** These slides refer to a legacy, pre–Spring Boot version of Petclinic and may not reflect the current Spring Boot–based implementation.  
> For up-to-date information, please refer to this repository and its documentation.


## Run Petclinic locally

Spring Petclinic is a [Spring Boot](https://spring.io/guides/gs/spring-boot) application built using [Maven](https://spring.io/guides/gs/maven/) or [Gradle](https://spring.io/guides/gs/gradle/).
Java 17 or later is required for the build, and the application can run with Java 17 or newer.

You first need to clone the project locally:

```bash
git clone https://github.com/spring-projects/spring-petclinic.git
cd spring-petclinic
```
If you are using Maven, you can start the application on the command-line as follows:

```bash
./mvnw spring-boot:run
```
With Gradle, the command is as follows:

```bash
./gradlew bootRun
```

You can then access the Petclinic at <http://localhost:8080/>.

<img width="1042" alt="petclinic-screenshot" src="https://cloud.githubusercontent.com/assets/838318/19727082/2aee6d6c-9b8e-11e6-81fe-e889a5ddfded.png">

You can, of course, run Petclinic in your favorite IDE.
See below for more details.

## Feature Flags

This application includes a feature flag system that allows you to enable or disable specific features at runtime without redeploying the application.

### How to Run the App

The application runs the same way as before. Feature flags are automatically initialized on startup with default values:

```bash
./mvnw spring-boot:run
```

Or with Gradle:

```bash
./gradlew bootRun
```

The application will be available at <http://localhost:8080/>.

### Assumptions and Design Decisions

1. **Database Persistence**: Feature flags are stored in the database (`feature_flags` table) to ensure persistence across application restarts. The schema is included for H2, MySQL, and PostgreSQL.

2. **Caching**: Feature flag status is cached using Spring Cache (Caffeine) to minimize database queries. The cache is automatically evicted when flags are updated.

3. **Default Flags**: Three feature flags are automatically created on startup if they don't exist:
   - `add_new_pet` (enabled by default)
   - `add_visit` (enabled by default)
   - `owner_search` (enabled by default)

4. **Helper Function**: The `FeatureFlagService.isEnabled(String flagName)` method can be called anywhere in the application to check flag status.

5. **Custom Annotation**: A `@FeatureFlagRequired` annotation is available for declarative feature flag checks using Aspect-Oriented Programming (AOP).

6. **Error Handling**: When a feature is disabled:
   - Controllers redirect users with an error message
   - The custom annotation can either throw an exception or return a default value

### Features with Flags

| Flag Name | What It Controls | Implementation Location |
|-----------|------------------|------------------------|
| `add_new_pet` | Controls the ability to add new pets to owners | `PetController.initCreationForm()` (line 102-111)<br>`PetController.processCreationForm()` (line 113-138)<br>**File**: `src/main/java/org/springframework/samples/petclinic/owner/PetController.java` |
| `add_visit` | Controls the ability to add visits for pets | `VisitController.initNewVisitForm()` (line 86-93)<br>`VisitController.processNewVisitForm()` (line 95-111)<br>**File**: `src/main/java/org/springframework/samples/petclinic/owner/VisitController.java` |
| `owner_search` | Controls the ability to search for owners by last name | `OwnerController.processFindForm()` (line 98-104)<br>**File**: `src/main/java/org/springframework/samples/petclinic/owner/OwnerController.java` |

When a feature flag is disabled:
- **Add New Pet**: Users are redirected to the owner details page with an error message
- **Add Visit**: Users are redirected to the owner details page with an error message
- **Owner Search**: The search form displays a validation error message

### API Documentation for Flag Management Endpoints

The feature flag management API is available at `/api/feature-flags`. All endpoints return JSON.

#### Get All Feature Flags

```http
GET /api/feature-flags
```

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "name": "add_new_pet",
    "enabled": true,
    "description": "Enable adding new pets",
    "createdAt": "2025-02-15T10:00:00",
    "updatedAt": "2025-02-15T10:00:00"
  }
]
```

#### Get Feature Flag by ID

```http
GET /api/feature-flags/{id}
```

**Response**: `200 OK` or `404 Not Found`

#### Get Feature Flag by Name

```http
GET /api/feature-flags/name/{name}
```

**Example**: `GET /api/feature-flags/name/add_new_pet`

**Response**: `200 OK` or `404 Not Found`

#### Check if Feature Flag is Enabled

```http
GET /api/feature-flags/check/{name}
```

**Example**: `GET /api/feature-flags/check/add_new_pet`

**Response**: `200 OK`
```json
true
```

#### Create Feature Flag

```http
POST /api/feature-flags
Content-Type: application/json
```

**Request Body**:
```json
{
  "name": "new_feature",
  "enabled": false,
  "description": "Description of the new feature"
}
```

**Response**: `201 Created` or `400 Bad Request` (if name already exists)

#### Update Feature Flag

```http
PUT /api/feature-flags/{id}
Content-Type: application/json
```

**Request Body**:
```json
{
  "name": "add_new_pet",
  "enabled": false,
  "description": "Updated description"
}
```

**Response**: `200 OK` or `404 Not Found` or `400 Bad Request` (if name conflicts)

#### Delete Feature Flag

```http
DELETE /api/feature-flags/{id}
```

**Response**: `204 No Content` or `404 Not Found`

#### Toggle Feature Flag

```http
PATCH /api/feature-flags/{id}/toggle
```

**Response**: `200 OK` with updated flag or `404 Not Found`

**Example**:
```bash
# Disable the add_new_pet feature
curl -X PATCH http://localhost:8080/api/feature-flags/1/toggle

# Enable it again
curl -X PATCH http://localhost:8080/api/feature-flags/1/toggle
```

### Using Feature Flags in Code

#### Method 1: Direct Service Call

```java
@Controller
public class MyController {
    
    private final FeatureFlagService featureFlagService;
    
    @GetMapping("/my-feature")
    public String myFeature() {
        if (!featureFlagService.isEnabled("my_feature")) {
            return "redirect:/error";
        }
        // Feature logic here
        return "myFeatureView";
    }
}
```

#### Method 2: Custom Annotation

```java
@RestController
public class MyController {
    
    @FeatureFlagRequired("my_feature")
    @GetMapping("/api/data")
    public ResponseEntity<Data> getData() {
        // This method will only execute if "my_feature" is enabled
        // Otherwise, it throws FeatureFlagDisabledException
        return ResponseEntity.ok(new Data());
    }
    
    @FeatureFlagRequired(value = "my_feature", throwException = false)
    @GetMapping("/api/optional")
    public ResponseEntity<Data> getOptionalData() {
        // If flag is disabled, returns null (or false for boolean)
        return ResponseEntity.ok(new Data());
    }
}
```

### Feature Flag Module Structure

The feature flag system consists of the following components:

- **Entity**: `FeatureFlag.java` - JPA entity for database persistence
- **Repository**: `FeatureFlagRepository.java` - Spring Data JPA repository
- **Service**: `FeatureFlagService.java` - Business logic and helper methods
- **Controller**: `FeatureFlagController.java` - REST API endpoints
- **Initializer**: `FeatureFlagInitializer.java` - Creates default flags on startup
- **Annotation**: `FeatureFlagRequired.java` - Custom annotation for AOP
- **Aspect**: `FeatureFlagAspect.java` - AOP implementation for annotation
- **Exception**: `FeatureFlagDisabledException.java` - Custom exception for disabled flags

## Building a Container

There is no `Dockerfile` in this project. You can build a container image (if you have a docker daemon) using the Spring Boot build plugin:

```bash
./mvnw spring-boot:build-image
```

## In case you find a bug/suggested improvement for Spring Petclinic

Our issue tracker is available [here](https://github.com/spring-projects/spring-petclinic/issues).

## Database configuration

In its default configuration, Petclinic uses an in-memory database (H2) which
gets populated at startup with data. The h2 console is exposed at `http://localhost:8080/h2-console`,
and it is possible to inspect the content of the database using the `jdbc:h2:mem:<uuid>` URL. The UUID is printed at startup to the console.

A similar setup is provided for MySQL and PostgreSQL if a persistent database configuration is needed. Note that whenever the database type changes, the app needs to run with a different profile: `spring.profiles.active=mysql` for MySQL or `spring.profiles.active=postgres` for PostgreSQL. See the [Spring Boot documentation](https://docs.spring.io/spring-boot/how-to/properties-and-configuration.html#howto.properties-and-configuration.set-active-spring-profiles) for more detail on how to set the active profile.

You can start MySQL or PostgreSQL locally with whatever installer works for your OS or use docker:

```bash
docker run -e MYSQL_USER=petclinic -e MYSQL_PASSWORD=petclinic -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=petclinic -p 3306:3306 mysql:9.5
```

or

```bash
docker run -e POSTGRES_USER=petclinic -e POSTGRES_PASSWORD=petclinic -e POSTGRES_DB=petclinic -p 5432:5432 postgres:18.1
```

Further documentation is provided for [MySQL](https://github.com/spring-projects/spring-petclinic/blob/main/src/main/resources/db/mysql/petclinic_db_setup_mysql.txt)
and [PostgreSQL](https://github.com/spring-projects/spring-petclinic/blob/main/src/main/resources/db/postgres/petclinic_db_setup_postgres.txt).

Instead of vanilla `docker` you can also use the provided `docker-compose.yml` file to start the database containers. Each one has a service named after the Spring profile:

```bash
docker compose up mysql
```

or

```bash
docker compose up postgres
```

## Test Applications

At development time we recommend you use the test applications set up as `main()` methods in `PetClinicIntegrationTests` (using the default H2 database and also adding Spring Boot Devtools), `MySqlTestApplication` and `PostgresIntegrationTests`. These are set up so that you can run the apps in your IDE to get fast feedback and also run the same classes as integration tests against the respective database. The MySql integration tests use Testcontainers to start the database in a Docker container, and the Postgres tests use Docker Compose to do the same thing.

## Compiling the CSS

There is a `petclinic.css` in `src/main/resources/static/resources/css`. It was generated from the `petclinic.scss` source, combined with the [Bootstrap](https://getbootstrap.com/) library. If you make changes to the `scss`, or upgrade Bootstrap, you will need to re-compile the CSS resources using the Maven profile "css", i.e. `./mvnw package -P css`. There is no build profile for Gradle to compile the CSS.

## Working with Petclinic in your IDE

### Prerequisites

The following items should be installed in your system:

- Java 17 or newer (full JDK, not a JRE)
- [Git command line tool](https://help.github.com/articles/set-up-git)
- Your preferred IDE
  - Eclipse with the m2e plugin. Note: when m2e is available, there is a m2 icon in `Help -> About` dialog. If m2e is
  not there, follow the installation process [here](https://www.eclipse.org/m2e/)
  - [Spring Tools Suite](https://spring.io/tools) (STS)
  - [IntelliJ IDEA](https://www.jetbrains.com/idea/)
  - [VS Code](https://code.visualstudio.com)

### Steps

1. On the command line run:

    ```bash
    git clone https://github.com/spring-projects/spring-petclinic.git
    ```

1. Inside Eclipse or STS:

    Open the project via `File -> Import -> Maven -> Existing Maven project`, then select the root directory of the cloned repo.

    Then either build on the command line `./mvnw generate-resources` or use the Eclipse launcher (right-click on project and `Run As -> Maven install`) to generate the CSS. Run the application's main method by right-clicking on it and choosing `Run As -> Java Application`.

1. Inside IntelliJ IDEA:

    In the main menu, choose `File -> Open` and select the Petclinic [pom.xml](pom.xml). Click on the `Open` button.

    - CSS files are generated from the Maven build. You can build them on the command line `./mvnw generate-resources` or right-click on the `spring-petclinic` project then `Maven -> Generates sources and Update Folders`.

    - A run configuration named `PetClinicApplication` should have been created for you if you're using a recent Ultimate version. Otherwise, run the application by right-clicking on the `PetClinicApplication` main class and choosing `Run 'PetClinicApplication'`.

1. Navigate to the Petclinic

    Visit [http://localhost:8080](http://localhost:8080) in your browser.

## Looking for something in particular?

|Spring Boot Configuration | Class or Java property files  |
|--------------------------|---|
|The Main Class | [PetClinicApplication](https://github.com/spring-projects/spring-petclinic/blob/main/src/main/java/org/springframework/samples/petclinic/PetClinicApplication.java) |
|Properties Files | [application.properties](https://github.com/spring-projects/spring-petclinic/blob/main/src/main/resources) |
|Caching | [CacheConfiguration](https://github.com/spring-projects/spring-petclinic/blob/main/src/main/java/org/springframework/samples/petclinic/system/CacheConfiguration.java) |

## Interesting Spring Petclinic branches and forks

The Spring Petclinic "main" branch in the [spring-projects](https://github.com/spring-projects/spring-petclinic)
GitHub org is the "canonical" implementation based on Spring Boot and Thymeleaf. There are
[quite a few forks](https://spring-petclinic.github.io/docs/forks.html) in the GitHub org
[spring-petclinic](https://github.com/spring-petclinic). If you are interested in using a different technology stack to implement the Pet Clinic, please join the community there.

## Interaction with other open-source projects

One of the best parts about working on the Spring Petclinic application is that we have the opportunity to work in direct contact with many Open Source projects. We found bugs/suggested improvements on various topics such as Spring, Spring Data, Bean Validation and even Eclipse! In many cases, they've been fixed/implemented in just a few days.
Here is a list of them:

| Name | Issue |
|------|-------|
| Spring JDBC: simplify usage of NamedParameterJdbcTemplate | [SPR-10256](https://github.com/spring-projects/spring-framework/issues/14889) and [SPR-10257](https://github.com/spring-projects/spring-framework/issues/14890) |
| Bean Validation / Hibernate Validator: simplify Maven dependencies and backward compatibility |[HV-790](https://hibernate.atlassian.net/browse/HV-790) and [HV-792](https://hibernate.atlassian.net/browse/HV-792) |
| Spring Data: provide more flexibility when working with JPQL queries | [DATAJPA-292](https://github.com/spring-projects/spring-data-jpa/issues/704) |

## Contributing

The [issue tracker](https://github.com/spring-projects/spring-petclinic/issues) is the preferred channel for bug reports, feature requests and submitting pull requests.

For pull requests, editor preferences are available in the [editor config](.editorconfig) for easy use in common text editors. Read more and download plugins at <https://editorconfig.org>. All commits must include a __Signed-off-by__ trailer at the end of each commit message to indicate that the contributor agrees to the Developer Certificate of Origin.
For additional details, please refer to the blog post [Hello DCO, Goodbye CLA: Simplifying Contributions to Spring](https://spring.io/blog/2025/01/06/hello-dco-goodbye-cla-simplifying-contributions-to-spring).

## License

The Spring PetClinic sample application is released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).
