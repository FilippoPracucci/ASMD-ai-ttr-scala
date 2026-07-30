package steps

import config.GameConfig.*
import config.Loader
import controller.{ClaimRouteController, DrawCardsController, TurnManager, ViewController}
import io.cucumber.scala.{EN, ScalaDsl}
import model.cards.{Card, Deck}
import model.map.{City, GameMap, Route}
import model.objective.ObjectiveWithCompletion
import model.player.Player
import model.utils.{Color, PlayerColor}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar.*
import view.GameView.City as CityName

import scala.collection.Seq

class PlayerTurnSteps extends ScalaDsl with EN with Matchers:
  private var deck: Deck = Deck()
  private var players: List[Player] = initPlayers()
  private var player = players.head
  private var turnManager = TurnManager(players)
  private val routes = Set(
    Route((City("Roma"), City("Venezia")), 6, Route.SpecificColor(Color.RED)),
    Route((City("Brindisi"), City("Palermo")), 3, Route.SpecificColor(Color.RED))
  )
  private val loader: Loader[Set[Route]] = () => routes
  private var gameMap = GameMap()(using loader)
  private var lastHand = player.hand
  private var lastTrainCars = player.trains

  private val viewController = mock[ViewController]
  private val drawCardsController = mock[DrawCardsController]
  private val claimRouteController = mock[ClaimRouteController]

  Before: _ =>
    deck = Deck()
    players = initPlayers()
    player = players.head
    turnManager = TurnManager(players)
    gameMap = GameMap()(using loader)
    lastHand = player.hand
    lastTrainCars = player.trains
    when(drawCardsController.drawCards()).thenAnswer: _ =>
      player.drawCards(StandardNumberOfCardsToDraw)
      lastHand = player.hand
    when(claimRouteController.claimRoute(any[(CityName, CityName)])).thenAnswer: (invocation: InvocationOnMock) =>
      val cities = invocation.getArgument[(CityName, CityName)](0)
      gameMap.getRoute(cities).foreach: route =>
        val routeColor = route.mechanic match
          case Route.SpecificColor(color) => color
          case _ => throw new IllegalStateException("Unhandled mechanic")
        if player.canPlayCards(routeColor, route.length) && player.canPlaceTrains(route.length) then
          gameMap.claimRoute(cities, player.id)
          player.playCards(routeColor, route.length)
          player.placeTrains(route.length)
          lastHand = player.hand
          lastTrainCars = player.trains

  ParameterType("color", Color.values.mkString("|"))(Color.valueOf)

  Given("the player is in turn"):
    turnManager.currentPlayer shouldBe player

  Given("the player has {int} {color} cards"): (n: Int, color: Color) =>
    while !player.canPlayCards(color, n) do
      player.drawCards(StandardNumberOfCardsToDraw)
    lastHand = player.hand
    player.hand.count(_.color == color) should be >= n

  Given("the player does not have {int} {color} cards"): (n: Int, color: Color) =>
    player.canPlayCards(color, n) shouldBe false

  Given("the player does not have {int} train cars"): (n: Int) =>
    player.placeTrains(player.trains - n + 1)
    lastTrainCars = player.trains
    player.canPlaceTrains(n) shouldBe false

  Given("the route from {string} to {string} is already claimed by another player"): (from: String, to: String) =>
    gameMap.claimRoute((from, to), players(1).id)
    gameMap.getPlayerClaimingRoute((from, to)) should be equals Right(Some(_))

  When("the player draws from the deck"):
    drawCardsController.drawCards()

  When("the player claims the route from {string} to {string}, with length {int} and color {color}"):
    (from: String, to: String, length: Int, color: Color) => claimRouteController.claimRoute((from, to))

  Then("the player's hand should be added with the {int} drawn cards"): (n: Int) =>
    player.hand should have size (HandInitialSize + n)

  Then("the player's hand should remain unchanged"):
    player.hand should have size HandInitialSize

  Then("the player should own the route from {string} to {string}"): (from: String, to: String) =>
    gameMap.getPlayerClaimingRoute((from, to)) should be(Right(Some(player.id)))

  Then("the player should not own the route from {string} to {string}"): (from: String, to: String) =>
    gameMap.getPlayerClaimingRoute((from, to)) should matchPattern{ case Right(id) if id != player.id => }

  And("the deck has at least {int} cards"): (n: Int) =>
    deck.cards.size should be >= n

  And("the deck has less than {int} cards"): (n: Int) =>
    deck.draw(deck.cards.size - n + 1)
    deck.cards.size should be < n

  And("the player should remain in turn forced to claim a route"):
    turnManager.currentPlayer shouldBe player

  And("the player has at least {int} train cars"): (n: Int) =>
    player.canPlaceTrains(n) shouldBe true

  And("the player's train cars should be reduced by {int}"): (n: Int) =>
    player.trains shouldBe (NumberTrainCars - n)

  And("the points for the route should be added to the player's score"):
    player.score should be equals PointsPerRouteLength(3)

  And("the {int} {color} cards played should be removed from the player's hand"): (n: Int, color: Color) =>
    lastHand.diff(player.hand) should be equals Seq.fill(n)(Card(color))

  And("the player's train cars should remain unchanged"):
    player.trains shouldBe lastTrainCars

  And("the player's score should remain unchanged"):
    player.score shouldBe InitialScore

  private def initPlayers(): List[Player] =
    var players: List[Player] = List.empty
    for
      color <- PlayerColor.values
    yield players +:= Player(color, deck, objective = ObjectiveWithCompletion(("Paris", "Berlin"), 8))
    players
