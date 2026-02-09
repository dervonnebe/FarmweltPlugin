# Maven to Gradle Migration Guide

## Overview
This project has been migrated from Maven to Gradle build system. This document explains the changes and how to work with the new build system.

## What Changed

### Build Files
- **Removed:** `pom.xml` (Maven configuration)
- **Added:** 
  - `build.gradle.kts` (Gradle build configuration using Kotlin DSL)
  - `settings.gradle.kts` (Gradle project settings)
  - `gradlew` and `gradlew.bat` (Gradle wrapper scripts)
  - `gradle/wrapper/` (Gradle wrapper configuration and JAR)

### Build Output Location
- **Maven:** `target/`
- **Gradle:** `build/libs/`

### CI/CD Pipeline
- GitHub Actions workflow updated to use Gradle
- Workflow now watches `build.gradle.kts` instead of `pom.xml` for version changes
- Build artifacts location changed from `target/` to `build/libs/`

## Version Management
The version is now defined in `build.gradle.kts`:
```kotlin
version = "1.4.3"
```

To update the version, edit this line in `build.gradle.kts`.

## Common Tasks

### Building the Project
```bash
# Clean and build
./gradlew clean build

# Build without running tests
./gradlew clean build -x test

# Only create the shadow JAR
./gradlew shadowJar
```

### Cleaning Build Artifacts
```bash
./gradlew clean
```

### Running Tasks
```bash
# List all available tasks
./gradlew tasks

# Get detailed help for a task
./gradlew help --task build
```

### IDE Integration

#### IntelliJ IDEA
IntelliJ IDEA automatically detects Gradle projects. Simply:
1. Open the project directory
2. IDEA will detect `build.gradle.kts` and import the project
3. Wait for dependency download and indexing to complete

#### Eclipse
1. Install the Buildship Gradle plugin (if not already installed)
2. File → Import → Gradle → Existing Gradle Project
3. Select the project directory and follow the wizard

#### VS Code
1. Install the "Gradle for Java" extension
2. Open the project directory
3. The extension will detect and configure the Gradle project

## Key Configuration Details

### Dependencies
All dependencies are maintained with the same versions:
- Spigot API 1.16.5-R0.1-SNAPSHOT (compileOnly)
- Multiverse-Core 5.1.0 (compileOnly)
- PlaceholderAPI 2.11.5 (compileOnly)
- bStats 3.0.2 (implementation, shaded)

### Shadow Plugin
The Gradle Shadow plugin replaces Maven Shade plugin:
- Automatically shades bStats into `top.jaxlabs.farmweltplugin.utils.bstats`
- Creates a fat JAR with all dependencies included
- Output: `build/libs/FarmweltPlugin-1.4.3.jar`

### Resource Filtering
The `plugin.yml` is still processed to replace `${project.version}` with the actual version.

## Advantages of Gradle

1. **Faster Builds:** Gradle's incremental build system is faster than Maven
2. **Better Dependency Management:** More flexible and powerful
3. **Kotlin DSL:** Type-safe configuration with IDE support
4. **Build Cache:** Can cache and reuse build outputs across projects
5. **Parallel Execution:** Better support for parallel task execution
6. **Modern Tooling:** Better integration with modern development tools

## Troubleshooting

### Gradle Daemon Issues
If you encounter issues with the Gradle daemon:
```bash
./gradlew --stop
./gradlew clean build
```

### Permission Issues (Linux/macOS)
If gradlew is not executable:
```bash
chmod +x gradlew
```

### Clean Build Cache
To completely clean the Gradle cache:
```bash
./gradlew clean
rm -rf .gradle/
./gradlew build
```

### Network/Dependency Issues
If dependencies fail to download:
```bash
./gradlew build --refresh-dependencies
```

## Migration Notes

- All Maven-specific files have been removed
- Build artifacts are now in `build/` instead of `target/`
- The `.gitignore` has been updated to ignore Gradle artifacts
- All functionality remains the same - this is purely a build system change
- The GitHub Actions workflow has been updated to work with Gradle

## Further Reading

- [Gradle User Guide](https://docs.gradle.org/current/userguide/userguide.html)
- [Gradle Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html)
- [Shadow Plugin Documentation](https://github.com/johnrengelman/shadow)
- [Migrating from Maven to Gradle](https://docs.gradle.org/current/userguide/migrating_from_maven.html)
