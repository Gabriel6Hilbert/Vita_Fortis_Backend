FROM node:22-alpine AS frontend-build

WORKDIR /workspace/frontend
RUN corepack enable

COPY frontend/package.json frontend/pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY frontend/ ./
RUN pnpm build


FROM maven:3.9-eclipse-temurin-17 AS backend-build

WORKDIR /workspace

COPY pom.xml ./
COPY .mvn/ .mvn/
COPY mvnw ./
COPY src/ src/
COPY --from=frontend-build /workspace/frontend/dist/ src/main/resources/static/

RUN mvn -DskipTests clean package


FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S vitafortis && adduser -S vitafortis -G vitafortis
COPY --from=backend-build --chown=vitafortis:vitafortis /workspace/target/demo-0.0.1-SNAPSHOT.jar app.jar

USER vitafortis
EXPOSE 5001

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
