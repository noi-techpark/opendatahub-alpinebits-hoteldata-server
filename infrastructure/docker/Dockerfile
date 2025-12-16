FROM maven:3.9-eclipse-temurin-25-alpine AS base

RUN mkdir -p /code
WORKDIR /code

# Dev
FROM base AS dev

# Build
FROM base AS build

COPY ./ /code
RUN ls -ls && mvn -DskipTests install

CMD ["mvn", "spring-boot:run", "-pl", "application-spring"]