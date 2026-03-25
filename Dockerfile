FROM maven:3.9-eclipse-temurin-18

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

EXPOSE 8000

CMD ["mvn", "exec:java", "-Dexec.mainClass=Main"]