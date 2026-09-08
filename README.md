# QuestShaper — Containerised Multiplayer Game Server

## The project overview

The project implements a Java HTTP server for a 2D tile-based adventure game running through QuestShaper. The server manages the world map, player sessions, player movement, multiplayer state, item pickup and placement, and tile interactions such as opening and closing doors.

The web client talks with the server via REST-style HTTP endpoints. The server runs on local port `8000`.
Designed for scalable cloud infrastructure, containerised with **Docker**, and automatically deployed to **AWS EC2 via Terraform and GitHub Actions**.

## Tech Stack

- **Language:** Java (JDK 18+)
- **HTTP Server Framework:** Java Native `com.sun.net.httpserver`
- **Containerisation:** Docker
- **Infrastructure as Code (IaC):** Terraform
- **Cloud Infrastructure:** AWS EC2 & Elastic IP
- **CI/CD Pipeline:** GitHub Actions
- **Testing:** JUnit 5 & Maven

## Features of the game

- **Session Management:** Secure token-based session lifecycle (`/login`, `/logout`) with custom SHA-256 password hashing.
- **Spatial Processing:** Real-time tile grid management, collision detection, boundary wrapping, and player occlusion checks.
- **Multiplayer State Engine:** Multi-tenant support allowing multiple active sessions to interact on the world grid simultaneously.
- **Dynamic Tile Stacking:** Interactive inventory management system (`/take`, `/place`, `/use`) supporting complex item/environment interactions (e.g., keys unlocking doors).
- **CORS-Enabled REST API:** Seamless integration with web-based frontend clients.

## System Architecture & Workflow

```text
  +------------------+         REST / JSON API          +--------------------+
  | QuestShaper Web  |  <---------------------------->  | Docker Container   |
  | Frontend Client  |                                  | (Java HTTP Server) |
  +------------------+                                  +--------------------+
                                                                  |
                                                         Deployed on AWS EC2
                                                         Provisioned via Terraform
```
## Authentication

The server uses a `USERS` environment variable to define valid users.

Each user is written as:

```text
username:password
```

Multiple users are separated with commas:

```powershell
$env:USERS="Tanvir:tanvir123"
```

The client sends the password as a SHA-256 hash of:

```text
name;password
```

For example, the password check for `Tanvir` uses the hash of:

```text
Tanvir;tanvir123
```

## Running the Project Locally

Open a PowerShell terminal in the project folder.

Set the allowed users:

```powershell
$env:USERS="Tanvir:tanvir123"
```

Compile the Java source files and start the server:

```powershell
javac -d out (Get-ChildItem src\main\java\*.java | ForEach-Object FullName)
java -cp out Main
```

If successful, the terminal should show:

```text
Server started on port 8000
```

## Running with Docker

Build the Docker image:

```powershell
docker build -t questshaper-server .
```

Run the container locally:

```powershell
docker run -p 8000:8000 -e USERS="Tanvir:tanvir123" questshaper-server
```

The server will be available at:

```text
http://localhost:8000
```

## Cloud Deployment

The project is deployed to an AWS EC2 instance using Terraform, Docker, and GitHub Actions.

Terraform is used to create the AWS infrastructure, including:

- an EC2 instance
- an Elastic IP for a stable server address
- a security group allowing SSH and server traffic on port 8000
- Docker installation on the EC2 instance

GitHub Actions is used to:

- run the Maven test suite
- build the Docker image
- push the image to Docker Hub
- connect to EC2 using SSH
- restart the Docker container with the latest image

The deployed server can be tested with:

```powershell
Invoke-WebRequest -UseBasicParsing "http://13.238.198.125:8000/move?dy=0&dx=0&session=fake"
```

A successful deployment should return HTTP `401 Unauthorized`, because the server is running and correctly rejecting an invalid session.

## Running Tests

When running in a Docker Container, use the following command:

```powershell
export USERS="Tanvir:tanvir123,Andre:andre123,Tahsin:tahsin123,Shaif:Shaif123"
mvn clean test
```

If running locally, run the automated JUnit tests with by declaring USERS and then using Maven:

```powershell
$env:USERS="Tanvir:tanvir123,Andre:andre123,Tahsin:tahsin123,Shaif:Shaif123,Marker:marker123,Guest:guest123"
mvn test
```

The GitHub Actions workflow also runs these tests automatically on pushes and pull requests to `main`.

## API Endpoints

### Login API - `POST /login`

This API Endpoint will log a player into the server and create a session.

- The request body must include the player's name and encrypted password using:

```json
{
  "name": "Tanvir",
  "encpswrd": "<sha256(name;password)>"
}
```

- Successful login returns HTTP 200 code with a session token:

```json
{
  "session": "SESSION"
}
```

- Missing fields return HTTP 400.
- Incorrect login details return HTTP 401.
- If the same username logs in again, the old session is removed and replaced by the new session.

### Logout API - `/logout?session=SESSION`

This API Endpoint will log the player out of the server.

- Successful logout returns HTTP 200 code.
- After logout, the session can no longer be used for other API requests.
- Invalid sessions return HTTP 401.

### Move API - `/move?dy=DY&dx=DX&session=SESSION`

This API Endpoint will move the player by one tile.

- Valid moves: up (N), down (S), left (W), right (E).
- Successful moves return HTTP 200 code to indicate the client request has been received, understood and processed with response body using:

```json
{
  "y": Y,
  "x": X
}
```

- Invalid or blocked moves return HTTP 204 to indicate the client request was received but returned with no response body.
- Invalid sessions return HTTP 401.

### Info API - `/info?y=Y&x=X&session=SESSION`

This API Endpoint will return map tile data around a location.

- Only valid if tile `(x, y)` matches the player's current location.
- Return HTTP 200 with:

```json
{
  "y": Y,
  "x": X,
  "top": T,
  "left": L,
  "bottom": B,
  "right": R,
  "info": [[...]]
}
```

- The `info` array may include stacked tile strings such as `g1`, `gk`, or `gk1`.
- Otherwise returns HTTP 204.
- Invalid sessions return HTTP 401.

### Take API - `/take?session=SESSION`

This API Endpoint will pick up an item from the player's current tile.

- Successful item pickup returns HTTP 200 code.
- If there is no item on the current tile, it returns HTTP 204.
- Invalid sessions return HTTP 401.

### Place API - `/place?session=SESSION`

This API Endpoint will place the player's currently held item onto the current tile.

- Successful item placement returns HTTP 200 code.
- If the player is not holding an item, it returns HTTP 204.
- If the current tile already contains a movable item, it returns HTTP 204.
- Invalid sessions return HTTP 401.

### Use API - `/use?dy=DY&dx=DX&session=SESSION`

This API Endpoint will use the player's held item or interact with a nearby tile.

- The `dy` and `dx` values choose the target tile relative to the player.
- For example, `dy=-1&dx=0` targets the tile above the player.
- Successful use returns HTTP 200 code.
- Invalid use actions return HTTP 204.
- Invalid sessions return HTTP 401.
- Current supported interaction: using a key on a door toggles it between closed `D` and open `d`.

## Map Format

The map is stored in:

```text
maps/world.txt
```

Each tile begins as a single character, but the server can store stacked tile strings during gameplay.

Example tile values:

- `g` - grass, walkable
- `_` - path or empty ground, walkable
- `W` - water, blocking
- `S` - stone or structure, blocking
- `B` - wall, blocking
- `D` - closed door, blocking
- `d` - open door, walkable
- `k` - key item
- `a` - axe item
- `c` - cyan potion item
- `h` - heart potion item
- `1`, `2`, `3` - player sprites

Stacked examples:

- `gk` - grass tile with a key
- `g1` - grass tile with player 1
- `wa` - wooden floor tile with an axe
- `gk1` - grass tile with a key and player 1

## Project Structure

```text
COMP3050-Project/
+-- src/
|   +-- main/java/
|   |   +-- Main.java
|   |   +-- GameState.java
|   |   +-- PlayerState.java
|   |   +-- SessionManager.java
|   |   +-- LoginHandler.java
|   |   +-- LogoutHandler.java
|   |   +-- MoveHandler.java
|   |   +-- InfoHandler.java
|   |   +-- TakeHandler.java
|   |   +-- PlaceHandler.java
|   |   +-- UseHandler.java
|   +-- test/java/
|       +-- InfoTest.java
|       +-- LoginTest.java
|       +-- LogoutTest.java
|       +-- MoveTest.java
|       +-- PlaceTest.java
|       +-- TakeTest.java
|       +-- UseTest.java
+-- maps/
|   +-- world.txt
+-- infra/
|   +-- main.tf
|   +-- terraform.tfvars.example
+-- .github/workflows/
|   +-- ci.yml
|   +-- deploy.yml
+-- Dockerfile
+-- pom.xml
+-- README.md
```

## QuestShaper Client

The server is designed to work with the QuestShaper version 3 web client:

```text
https://questshaper.com/version3/
```

Start the server locally, then connect the QuestShaper client to:

```text
http://localhost:8000
```

For the deployed AWS server, connect the client to:

```text
http://13.238.198.125:8000
```

If the official QuestShaper website blocks the deployed HTTP server because the website uses HTTPS, run the local copy of the client over HTTP instead:

```powershell
python -m http.server 5500
```

Then open:

```text
http://localhost:5500
```

Use the AWS server address in the local client:

```text
http://13.238.198.125:8000
```

This means the browser client is running locally, but all game API requests are still being sent to the deployed AWS server.

## Development Plan

### Phase 1 - Setup

- Created the GitHub repository
- Set up the Java HTTP server skeleton from week 1 workshop files
- Added the project folder structure
- Used workshop material to set up the initial `README.md`, base `Dockerfile`, and Maven `pom.xml`

### Phase 2 - Core Server Features

- Implemented the map loader using `maps/world.txt`
- Added shared game state through `GameState`
- Added per-player state through `PlayerState`
- Implemented the `/move` endpoint
- Implemented the `/info` endpoint
- Added movement validation and collision detection
- Added horizontal map wrapping

### Phase 3 - Authentication and Sessions

- Implemented `/login`
- Implemented `/logout`
- Added session token creation and validation
- Added the `USERS` environment variable for allowed login accounts
- Connected player state to each active session

### Phase 4 - Items and Interactions

- Implemented `/take`
- Implemented `/place`
- Implemented `/use`
- Added item pickup and item placement
- Added key and door interaction
- Updated map tiles to support stacked values such as `gk`, `g1`, and `gk1`

### Phase 5 - Multiplayer Support

- Added support for multiple active players
- Added different player sprites
- Added player overlays in the `/info` response
- Prevented players from moving onto occupied tiles
- Removed players from the game state after logout

### Phase 6 - Testing and Validation

- Added JUnit tests for login, logout, movement, info, take, place, and use endpoints
- Manually tested all API endpoints using PowerShell
- Tested the server with the QuestShaper version 3 web client
- Validated login, movement, info, take, place, use, and logout flows

### Phase 7 - DevOps and Finalisation

- Added GitHub Actions CI pipeline
- Added Docker support
- Added Terraform AWS infrastructure
- Added AWS EC2 deployment with Elastic IP
- Updated project documentation
- Cleaned up endpoint behaviour and HTTP status codes
- Prepared the project for demo, report, and presentation

## Team Workflow

- Use GitHub Issues for task tracking
- Each feature developed in a separate branch
- Use Pull Requests for code review
- Maintain clean commit history
- All team contact will be either in person or online on Discord

## Future improvements/changes to game

- Persistent save/load of game state
- Stronger password storage system such as Firebase
- More item types and interactions
- Larger maps
- Separate spawn positions for simultaneous multiplayer logins

## References

- Oracle Java documentation for `com.sun.net.httpserver.HttpServer`: https://docs.oracle.com/en/java/javase/18/docs/api/jdk.httpserver/com/sun/net/httpserver/HttpServer.html
- Docker documentation for building and running containers: https://docs.docker.com/
- GitHub Actions documentation: https://docs.github.com/en/actions
- Terraform AWS Provider documentation for EC2, security groups, and Elastic IP resources: https://registry.terraform.io/providers/hashicorp/aws/latest/docs
- AWS EC2 documentation for instances, security groups, Elastic IPs, and Docker deployment on Amazon Linux: https://docs.aws.amazon.com/ec2/

## AI Usage Disclosure

AI tools were used as support during development for:

- explaining assignment requirements and API behaviour
- debugging HTTP status code and session issues
- suggesting small code improvements and documentation wording
- helping design PowerShell smoke tests for the API endpoints

All AI-assisted suggestions were reviewed by the team before being added to the project. The final code, tests, deployment configuration, and documentation were manually checked against the assignment specification and tested with the QuestShaper client and PowerShell API requests.
