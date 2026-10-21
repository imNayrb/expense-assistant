# Build in due fasi: Maven compila il jar, l'immagine finale contiene solo la JRE.

FROM maven:3.9-eclipse-temurin-26 AS build
WORKDIR /app
# Prima solo il pom: le dipendenze restano in cache finché il pom non cambia.
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --no-create-home app
COPY --from=build /app/target/*.jar app.jar
# L'app non gira come root.
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
