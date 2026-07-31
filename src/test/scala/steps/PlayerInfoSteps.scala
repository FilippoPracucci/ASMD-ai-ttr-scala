package steps

import config.Loader
import controller.TurnManager
import io.cucumber.scala.{EN, ScalaDsl}
import model.cards.Deck
import model.map.{City, GameMap, Route}
import model.objective.ObjectiveWithCompletion
import model.player.Player
import model.utils.{Color, PlayerColor}
import org.scalatest.matchers.should.Matchers

class PlayerInfoSteps extends ScalaDsl with EN with Matchers:
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

  Before: _ =>
    deck = Deck()
    players = initPlayers()
    player = players.head
    turnManager = TurnManager(players)
    gameMap = GameMap()(using loader)

  Given("the map has been initialized with all routes"):
    gameMap.routes should contain allElementsOf routes

  Given("the player was assigned the objective"):
    player.objective shouldBe an[ObjectiveWithCompletion]

  Given("the player was dealt a hand of cards"):
    player.hand should not be empty

  Given("the player has a set of remaining train cars"):
    player.trains should be > 0

  Given("the players' scores have been initialized to {int}"): (score: Int) =>
    players.foreach(_.score should be equals score)

  When("the player is playing its turn"):
    turnManager.currentPlayer shouldBe player

  Then("the player should see all routes and their ownership status"):
    gameMap.routes.foreach: route =>
      gameMap.getPlayerClaimingRoute((route.connectedCities._1.name,
          route.connectedCities._2.name)) should be equals Right(None)

  Then("the player should see its objective as the cities {string} and {string} to connect"):
    (city1: String, city2: String) =>
      player.objective.citiesToConnect should be equals (city1, city2)

  Then("the player should see its hand containing {int} cards"): (cards: Int) =>
    player.hand.size should be equals cards

  Then("the player should see {int} train cars available"): (trainCars: Int) =>
    player.trains should be equals trainCars

  Then("the player should see the players' scores as a list of {int}"): (score: Int) =>
    players.map(_.score) should contain theSameElementsAs List.fill(players.length)(score)

  private def initPlayers(): List[Player] = PlayerColor.values.collect {
    case color => Player(color, deck, objective = ObjectiveWithCompletion(("Paris", "Berlin"), 8))
  }.toList
