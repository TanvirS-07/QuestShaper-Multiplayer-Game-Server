FROM eclipse-temurin:18-jdk

RUN apt-get update && apt-get install -y --no-install-recommends \
    maven \
    git \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Verify installations
RUN java --version && mvn --version && git --version

WORKDIR /workspace
