package controller

import dev.langchain4j.model.chat.ChatModel
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{atLeastOnce, doNothing, spy, verify}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.concurrent.Futures.timeout
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Seconds, Span}
import org.scalatestplus.mockito.MockitoSugar.mock

class AIPlayersTurnManagerTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach:
  import config.Loader
  import model.cards.Deck
  import model.player.Player
  import model.utils.PlayerColor
  import model.objective.ObjectiveWithCompletion
  import model.map.{GameMap, City, Route}
  import model.utils.Color

  private val mockModel = mock[ChatModel]
  private val gameController = mock[GameController]

  private val deck: Deck = Deck()
  private val routes = Set(
    Route((City("Roma"), City("Venezia")), 2, Route.SpecificColor(Color.BLACK)),
    Route((City("Roma"), City("Brindisi")), 3, Route.SpecificColor(Color.WHITE)),
    Route((City("Brindisi"), City("Palermo")), 4, Route.SpecificColor(Color.RED))
  )
  private val loader: Loader[Set[Route]] = () => routes
  private val gameMap = GameMap()(using loader)
  private val playersList: List[Player] = PlayerColor.values.collect {
    case color => Player(color, deck, objective = ObjectiveWithCompletion(("Paris", "Berlin"), 8))
  }.toList
  private val aiPlayersList: List[AIPlayer] = playersList.drop(1).map(player => AIPlayer(mockModel, player, gameMap))
  private var aiPlayerSpy: AIPlayer = spy(aiPlayersList.head)

  private var turnManager = TurnManager(playersList)
  private var aiPlayersTurnManager = AIPlayersTurnManager(aiPlayersList, turnManager, gameController)

  override def beforeEach(): Unit =
    doNothing().when(gameController).executeAIAction(any())
    aiPlayerSpy = spy(aiPlayersList.head)
    turnManager = TurnManager(playersList)
    aiPlayersTurnManager = AIPlayersTurnManager(aiPlayerSpy :: aiPlayersList.tail, turnManager, gameController)

  "An AI players turn manager" should "tell if the current player is an AI player" in:
    aiPlayersTurnManager.currentPlayerIsAI shouldBe false
    aiPlayersTurnManager.switchTurn()
    aiPlayersTurnManager.currentPlayerIsAI shouldBe true

  it should "handle the turn of an AI player" in:
    aiPlayersTurnManager.switchTurn()
    eventually(timeout(Span(2, Seconds))):
      verify(aiPlayerSpy).nextAction()
      verify(mockModel, atLeastOnce()).chat(anyString())
      verify(gameController, atLeastOnce()).executeAIAction(any())
