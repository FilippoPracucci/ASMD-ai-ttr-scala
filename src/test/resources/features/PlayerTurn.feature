Feature: Player draws cards from the deck or claims a route
  As a player,
  I want to be able to draw cards from the deck or claim a route
  so that I can end my turn.

  Scenario: Player draws successfully cards from the deck
    Given the player is in turn
    And the deck has at least 2 cards
    When the player draws from the deck
    Then the player's hand should be added with the 2 drawn cards

  Scenario: Player cannot draw cards because the deck is empty
    Given the player is in turn
    And the deck has less than 2 cards
    When the player draws from the deck
    Then the player's hand should remain unchanged
    And the player should remain in turn forced to claim a route

  Scenario: Player claims successfully a route
    Given the player has 3 RED cards
    And the player has at least 3 train cars
    When the player claims the route from 'Brindisi' to 'Palermo', with length 3 and color RED
    Then the player should own the route from 'Brindisi' to 'Palermo'
    And the 3 RED cards played should be removed from the player's hand
    And the player's train cars should be reduced by 3
    And the points for the route should be added to the player's score

  Scenario: Player cannot claim a route due to insufficient cards
    Given the player does not have 6 RED cards
    When the player claims the route from 'Roma' to 'Venezia', with length 6 and color RED
    Then the player should not own the route from 'Roma' to 'Venezia'
    And the player's hand should remain unchanged
    And the player's train cars should remain unchanged
    And the player's score should remain unchanged

  Scenario: Player cannot claim a route due to insufficient train cars
    Given the player does not have 6 train cars
    When the player claims the route from 'Roma' to 'Venezia', with length 6 and color RED
    Then the player should not own the route from 'Roma' to 'Venezia'
    And the player's hand should remain unchanged
    And the player's train cars should remain unchanged
    And the player's score should remain unchanged

  Scenario: Player cannot claim a route because it is already claimed by another player
    Given the route from 'Brindisi' to 'Palermo' is already claimed by another player
    When the player claims the route from 'Brindisi' to 'Palermo', with length 3 and color RED
    Then the player should not own the route from 'Brindisi' to 'Palermo'
    And the player's hand should remain unchanged
    And the player's train cars should remain unchanged
    And the player's score should remain unchanged
