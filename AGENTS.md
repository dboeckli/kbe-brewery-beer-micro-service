# AGENTS.md

Spring Boot 4 (parent 4.1.1) / Spring Framework 6 beer microservice on **Java 25** (enforced by the
maven-enforcer plugin). Single Maven module, package `ch.dboeckli.springframeworkguru.kbe.beer.services`.
It is a REST API (Spring Web MVC) for beers / breweries / orders, backed by MySQL (JPA) and Artemis
(JMS), with a gateway-style inventory integration (`kbe-brewery-inventory-micro-service` +
`kbe-brewery-inventory-failover`).

## Build & test commands

- Full build: `./mvnw clean verify` — format checks, unit (`*Test`, surefire) + IT (`*IT`, failsafe)
  tests, Helm lint/template. `./mvnw verify` also runs the unit tests.
- Unit tests only: `./mvnw test`. Single test: `./mvnw test -Dtest=BeerControllerTest#methodName`.
- `./mvnw clean install` additionally builds the Docker image and packages the Helm chart into
  `target/helm/repo/` (parent `*-chart-<version>.tgz` plus the `-jms-chart`/`-mysql-chart`
  subchart tgz). Skip the Docker build with `-Dskip.docker.build=true`.
- `-Dskip.start.stop.springboot=true` skips the in-build app boot (spring-boot:start/stop) that
  runs during the IT phase.
- Start locally: `./mvnw spring-boot:run` (app on `:8080`).

After changing code, always verify: run the relevant Maven goal above and report its output
(evidence, not just "done").

## Sandbox build quirk (background)

This sandbox mounts the repo via filesystem passthrough, which blocks symlinks — Spotless's
`npm install` (prettier) would fail with `EPERM` unless npm skips bin links. The sandbox kit sets
`npm_config_bin_links=false` globally (`spec.yaml` → `environment.variables`), so no manual export
is needed here. On a normal host (Windows/CI) this does not apply either.

## Formatting is enforced (fails the `validate` phase)

- Java: Spring Java Format → fix with `./mvnw spring-javaformat:apply`.
- Everything else (pom.xml, `**/*.md` except `AGENTS.md`/`CLAUDE.md`, json, application yaml,
  `**/*.sh`): Spotless → fix with `./mvnw spotless:apply`.

## External dependency gotcha

- DTOs and helpers come from the external module `ch.dboeckli...:kbe-brewery-lib` (GitHub Packages,
  `maven.pkg.github.com`). Without a PAT in `~/.m2/settings.xml` (server id `github`) the build
  cannot resolve dependencies.

## Test conventions

- Naming matters: `*Test` = unit (surefire), `*IT` = integration (failsafe). A `*Test` class will
  not run during `verify`'s failsafe phase and vice versa.
- ITs are `@SpringBootTest`; the build boots the app (spring-boot:start/stop) against the Docker
  Compose services defined in `compose.yaml` (mysql, jms, inventory apps) unless
  `-Dskip.start.stop.springboot=true`.

## Architecture

- Layered flow: `web/controllers` → `services` → `repositories` → `domain` entities.
- Mappers are MapStruct with the Lombok binding (`-Amapstruct.defaultComponentModel=spring`).
- JMS listener services react to brewing / order validation events (Artemis).
- RestClient (`spring-boot-starter-restclient`) talks to the inventory services
  (`sfg.brewery.beer-inventory-service-host`, `...inventory-failover-service-host`).

## Running locally

- `compose.yaml` is auto-started via `spring.docker.compose` on boot (mysql :3306, jms :61616/:8161,
  inventory-service :8082, inventory-failover :8083).
- Artemis console: http://localhost:8161/console
- Manual API testing: IntelliJ HTTP files in `restRequest/`.

## Deploy / CI

- Deployment is Helm-only: chart in `helm-charts/` (name `kbe-brewery-beer-micro-service-chart`),
  packaged to `target/helm/repo/`, release name = `kbe-brewery-beer-micro-service`, namespace
  `kbe-brewery-beer-micro-service`. A `k8s/` source dir no longer exists — deploy via Helm only
  (see README, `.run/` scripts).
- CI (`.github/workflows/`): `maven-build.yml` builds + deploys snapshots and triggers
  `deploy-and-test-cluster.yml`; `release.yml` runs `mvn release:prepare release:perform` on
  main/master only (version must be `-SNAPSHOT`); SonarCloud analysis runs in the `analyze` job.
- Dependency updates are managed via `.github/dependabot.yml` (actions) and `.github/renovate.json`;
  validate changes with `renovate-config-validator`.
