# 1. Build Stage
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Optimize Layer Caching: Copy Gradle Wrapper/Config first
COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .

# Grant execution rights and download dependencies (offline cached if possible)
RUN chmod +x ./gradlew
# 'build' or 'goOffline' can be used here. 
# Using a dummy build or just copying files next is common. 
# For simplicity and robustness, we skip a dedicated 'goOffline' step as it can be flaky with some plugins.

# Copy Source
COPY src src

# Build (Skip tests for speed/CI, assume CI runs tests if configured)
RUN ./gradlew clean build -x test --no-daemon

# 2. Runtime Stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# JVM Options Explanation:
# -XX:+UseContainerSupport: Make JVM aware of container limits (cgroup v1/v2)
# -UseSerialGC: Best for small heaps (low overhead, low footprint) vs G1GC/Parallel
# -Xmx256m: Strict Max Heap (256MB). Leaves 256MB for Native Mem, Stack, Metaspace, OS overhead
# -Xss512k: Reduce thread stack size (default 1MB) to save native memory
# -XX:ReservedCodeCacheSize=64M: Limit JIT compilation cache
# -XX:MaxMetaspaceSize=128m: Limit class metadata memory
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:+UseSerialGC -Xmx256m -Xss512k -XX:ReservedCodeCacheSize=64M -XX:MaxMetaspaceSize=128m -Dserver.port=8080 -Dspring.profiles.active=prod -jar app.jar"]