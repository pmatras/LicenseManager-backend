FROM gradle:jdk11 AS build_stage
RUN mkdir -p /license-manager-backend
WORKDIR /license-manager-backend
COPY . /license-manager-backend
RUN gradle build

FROM openjdk:11.0.8-jre-slim
COPY --from=build_stage /license-manager-backend/build/libs/*.jar license-manager-backend.jar
EXPOSE 9800
ENTRYPOINT [ "java", "-jar", "license-manager-backend.jar" ]