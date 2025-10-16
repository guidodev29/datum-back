# Etapa 1: Build de la aplicación
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar archivos de Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Descargar dependencias
RUN mvn dependency:go-offline

# Copiar el código fuente
COPY src ./src

# Compilar la aplicación
RUN mvn clean package -DskipTests 
#RUN mvn clean package -Dmaven.test.skip=true

# Etapa 2: Imagen de runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar el jar compilado
COPY --from=build /app/target/quarkus-app/lib/ /app/lib/
COPY --from=build /app/target/quarkus-app/*.jar /app/
COPY --from=build /app/target/quarkus-app/app/ /app/app/
COPY --from=build /app/target/quarkus-app/quarkus/ /app/quarkus/

# Exponer el puerto
EXPOSE 8082

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "/app/quarkus-run.jar"]
