# Picard Project Agent Guide

## Commands
- Build: `./gradlew build`
- Test all: `./gradlew test`
- Single module test: `./gradlew :core:test`
- Single test class: `./gradlew :core:test --tests "io.violabs.konstellation.domain.SomeTest"`
- Coverage: `./gradlew koverHtmlReport` or `./gradlew :core:test koverHtmlReport -Pkover`

## Code Style & Conventions
- Kotlin DSL-based builder pattern with scope functions
- Naming: `ResourceNameBuilder` for builder classes, `*Test.kt` for test files
- Use extension functions over utility classes
- For validation, use `vRequireNotNull` and `vRequireNotEmpty` functions
- Error handling: Log first, then throw with descriptive messages
- Testing: Use `SuccessBuildSim`, `FailureBuildSim`, or `FullBuildSim` base classes

## Project Structure
- Core domain model in `core/` directory
- Tests in `src/test/kotlin` mirroring main source structure
- Builder pattern with reflection for validation