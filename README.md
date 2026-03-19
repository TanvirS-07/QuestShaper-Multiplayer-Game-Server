# COMP3050 Project - 2D Tile-Based Virtual Server

## The project overview

The project implements a server for a 2D tile-based adventure game. This server will be responsible for managing the map, tracking the player's position, and utilising HTTP endpoints for player movement and game information. A provided web client will communicate with the server in order to render the game.

## Tech Stack

Language: Java (JDK 18+)
HTTP Server: Built-in Java HTTP Server (from Week1)
Data Storage: Text-based map file loaded into memory
Containerisation: Docker Desktop
Testing: JUnit 5
CI/CD: GitHub Actions
Version Control: Git + GitHub

## Features of the game (Unfinished)

- Tile-based map loaded from a text file
- Player movement system (N/S/E/W)
- Tiles that block movement (Collision system)
- REST API

## API Endpoints

### /move?dy=DY&dx=DX
This API Endpoint will move the player by one tile.
- Valid moves: up (N), down(S), left(W), right(E)
- Successful moves return HTTP 200 code to indicate client request has been recieved, understood and processed with response body using:
{ "y": Y, "x": X }
- Invalid or blocked moves return HTTP 204 to indicate client request was recieved but returned with no response body

### /info?y=Y&x=X
This API Endpoint will return map tile data around a location.
- Only valid if tile (x, y) matches player location
- Return HTTP 200 with:
{
  "y": Y,
  "x": X,
  "top": T,
  "left": L,
  "bottom": B,
  "right": R,
  "info": [[...]]
}
- Otherwise returns HTTP 204

## Map Format (Unfinished)

The map will be stored as a text file, and loaded into memory as a 2D array.
Some example tiles will include:
- g = Grass (Free to move)
- B = Wall (blocking)
- W = Water (blocking)
- _ = Empty Space (Free to move)

## Project Structure (Unfinished)
COMP3050 Project/
├── src/ 
│ ├── main/java/ 
│ └── test/java/ 
├── maps/ 
│ └── world.txt 
├── Dockerfile 
├── .github/workflows/ 
├── README.md

## Development Plan (Unfinished)
### Phase 1 – Setup
- Create repository
- Set up Java server skeleton
- Add Dockerfile (basic)
- Define project structure
- Create README.md

### Phase 2 – Add core functions
- Implement map loader
- Implement player state
- Implement /move endpoint
- Implement /info endpoint

### Phase 3 – Testing & Validation
- Add unit tests for movement and map logic
- Validate API responses
- Handle edge cases (bounds, invalid input) using JUnit tests

### Phase 4 – DevOps
- Add GitHub Actions CI pipeline
- Ensure Docker build works
- Improve logging and error handling

### Phase 5 – Finalisation
- Restructure code for clarity
- Complete documentation
- Prepare demo and presentation

## Team Workflow (Unfinished)
- Use GitHub Issues for task tracking
- Each feature developed in a separate branch
- Use Pull Requests for code review
- Maintain clean commit history
- All team contact will be either in person or online on Discord

## Running the Project (Unfinished)
*Locally*
./gradlew run
*Docker*
docker build -t game-server . 
docker run -p 8000:8000 game-server

## Future improvements/changes to game
- Add more tile types and interactions
- Support multiple players
- Improved larger map generation
- Add persistent game state

## AI Usage Disclosure
Our team recognises that the project allows the use of AI tools for:
- Code suggestions
- Debugging assistance
- Documentation support

The team will utilise these AI tools but make sure to review and validate all generated content before using it in the project.