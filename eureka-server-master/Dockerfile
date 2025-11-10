
# =========================
# STAGE 1: BUILD (Maven)
# =========================
FROM eclipse-temurin:21-jdk-jammy AS build
ARG DEBIAN_FRONTEND=noninteractive
WORKDIR /workspace

RUN apt-get update \
 && apt-get install -y --no-install-recommends \
      maven ca-certificates  \
 && rm -rf /var/lib/apt/lists/*

# Cachear dependencias de Maven
COPY pom.xml .
RUN mvn -q -e -B -DskipTests dependency:go-offline

# Compilar el proyecto
COPY src ./src
RUN mvn -q -e -B -DskipTests clean package



# =========================
# STAGE 2: RUNTIME
# =========================
FROM eclipse-temurin:21-jre-jammy
WORKDIR /opt/app

# Copiar la aplicación compilada
COPY --from=build /workspace/target/*.jar /opt/app/app.jar

RUN useradd -ms /bin/bash appuser
USER appuser

EXPOSE 8081
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
#docker network create red_paradigmas
#docker  build -t  eureka .
#docker run --name eureka --network red_paradigmas -p 8761:8761 eureka
#docker login
#docker tag micro  allamchz/eureka:latest
#docker push allamchz/eureka:latest

