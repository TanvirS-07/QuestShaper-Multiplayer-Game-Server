FROM eclipse-temurin:18-jdk

WORKDIR /app

COPY . .

EXPOSE 8000

# Replace this later with your actual build/run command
CMD ["java", "-version"]