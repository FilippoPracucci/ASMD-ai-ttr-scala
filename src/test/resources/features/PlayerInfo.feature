Feature: Player sees the game state
  As a player,
  I want to be able to see the current game state
  so that I can make informed decisions.

  Scenario: Player sees the current map state
    Given the map has been initialized with all routes
    When the player is playing its turn
    Then the player should see all routes and their ownership status

  Scenario: Player sees its own objectives
    Given the player was assigned the objective
    When the player is playing its turn
    Then the player should see its objective as the cities 'Paris' and 'Berlin' to connect

  Scenario: Player sees its own hand of cards
    Given the player was dealt a hand of cards
    When the player is playing its turn
    Then the player should see its hand containing 4 cards

  Scenario: Player sees its own remaining train cars
    Given the player has a set of remaining train cars
    When the player is playing its turn
    Then the player should see 45 train cars available

  Scenario: Player sees all players' scores
    Given the players' scores have been initialized to 0
    When the player is playing its turn
    Then the player should see the players' scores as a list of 0
