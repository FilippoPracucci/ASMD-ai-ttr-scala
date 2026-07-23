package controller

import dev.langchain4j.model.chat.ChatModel
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{atLeastOnce, doNothing, spy, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock

class AIPlayersTurnManagerTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach:
  import config.Loader
  import model.cards.Deck
  import model.player.Player
  import model.utils.PlayerColor
  import model.objective.ObjectiveWithCompletion
  import model.map.{GameMap, City, Route}
  import model.utils.Color

  private val mockModel: ChatModel = mock[ChatModel]
  private val deck: Deck = Deck()
  private val routes = Set(
    Route((City("Roma"), City("Venezia")), 2, Route.SpecificColor(Color.BLACK)),
    Route((City("Roma"), City("Brindisi")), 3, Route.SpecificColor(Color.WHITE)),
    Route((City("Brindisi"), City("Palermo")), 4, Route.SpecificColor(Color.RED))
  )
  private val loader: Loader[Set[Route]] = () => routes
  private val gameMap = GameMap()(using loader)

  private var playerList: List[Player] = List.empty
  for
    color <- PlayerColor.values
  yield playerList +:= Player(color, deck, objective = ObjectiveWithCompletion(("Paris", "Berlin"), 8))

  private var aiPlayersList: List[AIPlayer] = List.empty
  for
    player <- playerList
    if player.id != playerList.head.id
  yield aiPlayersList :+= AIPlayer(mockModel, player, gameMap)
  private var aiPlayerSpy: AIPlayer = spy(aiPlayersList.head)

  private val gameController = mock[GameController]
  private var turnManager = TurnManager(playerList)
  private var aiPlayersTurnManager = AIPlayersTurnManager(aiPlayersList, turnManager, gameController)

  override def beforeEach(): Unit =
    doNothing().when(gameController).executeAIAction(any())
    aiPlayerSpy = spy(aiPlayersList.head)
    turnManager = TurnManager(playerList)
    aiPlayersTurnManager = AIPlayersTurnManager(aiPlayerSpy :: aiPlayersList.tail, turnManager, gameController)

  "An AI players turn manager" should "tell if the current player is an AI player" in:
    aiPlayersTurnManager.currentPlayerIsAI shouldBe false
    aiPlayersTurnManager.switchTurn()
    aiPlayersTurnManager.currentPlayerIsAI shouldBe true

  it should "handle the turn of an AI player" in:
    aiPlayersTurnManager.switchTurn()
    verify(aiPlayerSpy).nextAction()
