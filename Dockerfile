FROM eclipse-temurin:18-jdk

WORKDIR /app

COPY src/main/java/ ./
COPY maps/ ./maps/

RUN javac *.java

EXPOSE 8000

CMD ["java", "Main"]