# ARCHITECTURE

## Overview
The project follows **MVC** as architectural pattern with a clear separation of concerns:
- **Model**: game rules and domain state;
- **Controller**: orchestrates game use cases;
- **View**: graphical interface and user interactions.

`src/main/scala/Launcher.scala` is the entrypoint, launching the singleton `GameController`, which builds the
components, initializes the game and links the UI to the logic.

## Project structure

### Modules

- `config`: configuration and resource loading utilities;
- `model`: domain model and logic (cards, map, objectives, players);
- `controller`: orchestrates game use cases and turn management by means of specialized controllers;
- `view`: graphical interface and user interactions.

### Main components

#### Model

- `GameMap`: represents the game map, composed of cities linked by routes;
- `Deck`: represents the deck of train cards;
- `Player`: represents a player.

#### Controller

- `GameController`: the main controller of the game, containing also the `TurnManager`;
- `DrawCardsController`: handles the "draw cards" use case;
- `ClaimRouteController`: handles the "claim route" use case;
- `TurnManager`: handles the game state by managing the turn order and the current player.

#### View

- `GameView`: represents the view of the game and contains all the information to play.

### Important locations

- `docs/report/`: contains the documentation (in Italian);
- `.github/workflows/`: contains the CI/CD pipelines definitions;
- `build.sbt`: sbt build configuration.

### Dependencies

- The programming language used is **Scala3**;
- `scala-swing`: wrapper of _Java Swing_ to build the GUI;
- `alice.tuprolog`: in order to use Prolog for checking objectives completion;
- `JGraphX`: library based on _Java Swing_ to draw graphs, in order to represent the map;
- `ujson`: library to parse JSON files;
- `langchain4j`: library to integrate LLMs;
- `scalatest`: library to write unit tests;
- `mockito`: library to create mocks and writing integration tests.
