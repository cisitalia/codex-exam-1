# sb (Spring Boot)

Java 21 + Spring Boot 3.3.5 project using Gradle Kotlin DSL.

## Tech Stack
- Java 21 (Microsoft Build recommended)
- Spring Boot 3.3.5
- Gradle (Kotlin DSL)
- Thymeleaf, MyBatis
- MySQL 5.7 (H2 optional for local dev)

## Package
- Group: `kr.co.ldk`
- Artifact: `sb`
- Base package: `kr.co.ldk.sb`

## Run
1. Create a `.env` (see `.env.example`).
2. Ensure MySQL is reachable and DB exists (default: `ldk_common`).
3. Use Gradle to run:
   - With local Gradle: `gradle bootRun`
   - With wrapper (after generating wrapper locally): `./gradlew bootRun`

> Note: Wrapper binaries are not included. Run `gradle wrapper` locally to generate them.

## Environment
`.env` values read via `spring-dotenv`:
- `DB_HOST` (default: `localhost`)
- `DB_PORT` (default: `3306`)
- `DB_NAME` (default: `ldk_common`)
- `DB_USERNAME`
- `DB_PASSWORD`

## Endpoints
- `GET /` — Renders a Thymeleaf page.

