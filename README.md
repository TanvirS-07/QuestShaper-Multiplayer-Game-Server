# COMP3050 Project - 2D Tile-Based Virtual Server

## The project overview

The project implements a Java HTTP server for a 2D tile-based adventure game running through QuestShaper. The server manages the world map, player sessions, player movement, multiplayer state, item pickup and placement, and tile interactions such as opening and closing doors.

The web client talks with the server via REST-style HTTP endpoints. The server runs on the local port '8000'.

## Tech Stack

Language: Java (JDK 18+)
HTTP Server: The Java built-in 'com.sun.net.httpserver.HttpServer'
Data Storage:  Text-based map loaded from `maps/world.txt`
Containerisation: Docker Desktop
Testing: JUnit 5
CI/CD: GitHub Actions
Version Control: Git + GitHub Desktop

## Features of the game

- Tile-based map loaded from `maps/world.txt`
- Authenticated login and logout system
- Session-based player states
- Multiple players supported at the same time
- Different player sprites displayed on the map (skins)
- Player movement using north, south, east, and west directions
- Collision detection for blocking tiles
- Item pickup using `/take`
- Item placement using `/place`
- Item and tile interaction using `/use`
- CORS headers for QuestShaper browser access

## Authentication

The server uses a `USERS` environment variable to define valid users.

Each user is written as:
username:password

Multiple users are separated with commas:
```powershell
$env:USERS="Tanvir:tanvir123,Andre:andre123,Tahsin:tahsin123,Shaif:shaif123"
```

## Running the Project Locally

Open a PowerShell terminal in the project folder.

Set the allowed users:
```powershell
$env:USERS="Tanvir:tanvir123,Andre:andre123,Tahsin:tahsin123,Shaif:shaif123"
```

Compile the Java source files and start the file:
```powershell
javac -d out (Get-ChildItem src\main\java\*.java | ForEach-Object FullName)
java -cp out Main
```

If successful, the terminal should show: Server started on port 8000

## Running with Docker
TODO

## Running Tests

Run the automated JUnit tests with Maven:
```powershell
mvn test
```

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

### Logout API - `/logout?session=SESSION`
This API Endpoint will log the player out of the server.
- Successful logout returns HTTP 200 code.
- After logout, the session can no longer be used for other API requests.

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
- Invalid sessions return HTTP 401.

### Use API - `/use?dy=DY&dx=DX&session=SESSION`
This API Endpoint will use the player's held item or interact with a nearby tile.
- The `dy` and `dx` values choose the target tile relative to the player.
- For example, `dy=-1&dx=0` targets the tile above the player.
- Successful use returns HTTP 200 code.
- Invalid use actions return HTTP 204.
- Invalid sessions return HTTP 401.
- Current supported interaction: using a key on a closed door changes it from `D` to `d`.


## Map Format (Unfinished)

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
- `c` - collectible or consumable item
- `h` - collectible or consumable item
- `1`, `2`, `3` - player sprites

Stacked examples:

- `gk` - grass tile with a key
- `g1` - grass tile with player 1
- `wa` - floor tile with an axe
- `gk1` - grass tile with a key and player 1


## Project Structure

```text
COMP3050-Project/
├── src/
│   ├── main/java/
│   │   ├── Main.java
│   │   ├── GameState.java
│   │   ├── PlayerState.java
│   │   ├── SessionManager.java
│   │   ├── LoginHandler.java
│   │   ├── LogoutHandler.java
│   │   ├── MoveHandler.java
│   │   ├── InfoHandler.java
│   │   ├── TakeHandler.java
│   │   ├── PlaceHandler.java
│   │   └── UseHandler.java
│   └── test/java/
│       └── MainTest.java
├── maps/
│   └── world.txt
├── .github/workflows/
│   └── ci.yml
├── Dockerfile
├── pom.xml
└── README.md
```

## QuestShaper Client

The server is designed to work with the QuestShaper version 3 web client.
https://questshaper.com/version3/
Start the server locally, then connect the QuestShaper client to:

```text
http://localhost:8000
```

## Development Plan 

### Phase 1 - Setup
- Created the GitHub repository
- Set up the Java HTTP server skeleton from week1 workshop files
- Added the project folder structure
- Using workshop files setup the follow:
- Initial `README.md`
- Base `Dockerfile`
- Maven `pom.xml`

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
- Added JUnit tests for map loading and movement logic
- Added tests for blocking tiles and item helper logic
- Manually tested all API endpoints using PowerShell
- Tested the server with the QuestShaper version 3 web client
- Validated login, movement, info, take, place, use, and logout flows

### Phase 7 - DevOps and Finalisation
- Added GitHub Actions CI pipeline
- Added Docker support
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

## AI Usage Disclosure
Our team recognises that the project allows the use of AI tools for:
- Code suggestions
- Debugging assistance
- Documentation support

The team will utilise these AI tools but make sure to review and validate all generated content before using it in the project.
