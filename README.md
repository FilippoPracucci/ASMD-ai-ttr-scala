# ASMD-ai-ttr-scala

The application is based on [PPS-24-ttr-scala](https://github.com/FilippoPracucci/PPS-24-ttr-scala.git), to which has
been integrated an LLM to play the game as an AI player. In addition, the development has exploited AI-assisted
software engineering techniques. Finally, some acceptance tests have been implemented.

## ASMD tasks

The tasks that have been implemented can be found in the branch `feature/ai-player` and are the following:

### Lab 01 task 3 ("_REQUIRE_")
The task consists in the writing of the Gherkin specification to capture the requirements of the application. So, I
wrote the specification in the `resources/features` folder, where i divided the requirements into:
- `PlayerTurn.feature` in which there are the scenarios that can happen during the turn of a player, so the actions of
drawing cards and claiming routes, either successfully or unsuccessfully;
- `PlayerInfo.feature` that concerns what a player has to be able to see and verify during the game.

After that I wrote the steps exploiting _Cucumber_ in order to implement the scenarios and verify their satisfaction.

### Lab 02 task 2 ("_REENGINEER_")
I wrote some integration tests exploiting mocking through Mockito:
- mock of the `ChatModel` in `AIPlayerTest` to instruct some stubbed replies when the `chat` method is called, other
than to verify the actual invocation of the method;
- mocks of `ChatModel` and `GameController` in `AIPlayersTurnManagerTest` to verify the invocations of the right methods
and define the right behavior of the `executeAIAction` method. In addition, I insert a wrapper to an AI player through
`spy` in order to detect its events;
- mocks of `DrawCardsController` and `ClaimRouteController` in `PlayerTurnSteps` to define relatively the behavior of
`drawCards` and `claimRoute` methods.

### Lab 03 task 3 ("_AI-APP DESIGN_")
The task consists in the implementation of an AI player that can play the game. In order to do so, I created the
`AIPlayer` which mainly implement a `nextAction` method which makes requests to a `ChatModel` by a prompt defined in
`AIPlayerPrompt`. Once the response is received, it is parsed by an `AIPlayerResponseParser` and a Future containing the
action to be executed, with some optional parameters, is returned. This procedure can be retried in case of failure, up
to a maximum number of 3 attempts. The prompt exploited to instruct the model contains a brief description of the game
and its goal; in addition, the specific player objective, hand, available and owned routes are provided.
Other adjustments have been made in particular to the `GameController`, and in some other controllers, in order to allow
the AI player to play its turn and an `AIPlayersTurnManager` has been created to manage the turn of the AI player.
The model used is `qwen3.5:4b` run locally through Ollama.

### Lab 03b task 1/2
The development of the previous task in which I added the notion of AI player, has been done exploiting AI-assisted
software engineering techniques. In particular, I used copilot with `copilot-instructions.md`, `ARCHITECTURE.md`,
`CONTRIBUTING.md` and `PRODUCT.md` files. Then I created two agents:
- `tdd.agent` to draft a first implementation given the unit test to pass;
- `scaladoc.agent` to write the Scaladoc.

The workflow that I followed is:
1. I wrote the unit test for a method to implement;
2. I run the `tdd.agent` to generate a first implementation of the method;
3. I refactored the code;
4. I run the `scaladoc.agent` to generate the Scaladoc for the method;
5. I refined the Scaladoc if necessary.

What I noticed is that the TDD agent is able to generate a draft of the first implementation of the method, but it is
not always correct and in particular it is often a really basic implementation, which requires quite an amount of
refactoring. In order to improve the quality of the generated code is probably necessary to exploit a better model,
but the cost for the requests would have been too high, so I decided to use the "auto" mode.

## Description

**Ticket to Ride** is a board game in which four players compete to obtain points, that are gained by claiming
routes and completing objectives. The board represents the Europe map composed by a set of cities, connected by
railway routes characterized by colors. During a turn a player can draw 2 cards from the deck or claim a route.
A player in order to claim a route has to play cards of the color of the route.
Each player has 45 train cars, which are placed to claim routes and when a player remains with less than 3 train cars
starts the last round. At the end of the game the player with the most points wins.

## How to run

In order to run the game is necessary to download the `jar` from the latest release and execute it, for example running
the following command via CLI:

```
java -jar ttr-scala.jar
```

## Documentation

The project documentation is visible at [docs](https://filippopracucci.github.io/PPS-24-ttr-scala/).

## Authors

- [Federica Bedeschi](https://github.com/BedeschiFederica)
- [Filippo Pracucci](https://github.com/FilippoPracucci)
