package controller

import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock

class AIPlayerTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach:
  import dev.langchain4j.model.chat.ChatModel
  import config.Loader
  import model.map.{City, GameMap, Route}
  import model.player.Player
  import model.utils.Color
  import model.objective.{ObjectiveCompletion, ObjectiveWithCompletion}
  import model.utils.PlayerColor
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.util.Success

  private val TimeoutMillis = 5000
  private val PlayerId = PlayerColor.GREEN
  private val ErrorMessage = "AIPlayer.nextAction failed to return a valid action"

  private val mockModel: ChatModel = mock[ChatModel]
  private val routes = Set(
    Route((City("Roma"), City("Venezia")), 2, Route.SpecificColor(Color.BLACK)),
    Route((City("Roma"), City("Brindisi")), 3, Route.SpecificColor(Color.WHITE)),
    Route((City("Brindisi"), City("Palermo")), 4, Route.SpecificColor(Color.RED))
  )
  private val loader: Loader[Set[Route]] = () => routes
  private var gameMap: GameMap = GameMap()(using loader)
  private val objective: ObjectiveCompletion = ObjectiveWithCompletion(("Roma", "Palermo"), 20)
  private val player: Player = Player(playerId = PlayerId, objective = objective)
  private var aiPlayer: AIPlayer = AIPlayer(mockModel, player, gameMap)

  override def beforeEach(): Unit = {
    gameMap = GameMap()(using loader)
    aiPlayer = AIPlayer(mockModel, player, gameMap)
  }

  "An AIPlayer" should "return parsed choice of drawing cards" in:
    when(mockModel.chat(anyString())).thenReturn(s"{\"action\": \"${AIPlayerAction.DRAW_CARDS.action}\"}")
    aiPlayer.nextAction.onComplete:
      case Success(action) => action._1 should be(AIPlayerAction.DRAW_CARDS)
      case _ => fail(ErrorMessage)

  it should "return parsed choice of claiming a route" in:
    when(mockModel.chat(anyString())).thenReturn(s"""
      |{"action": \"${AIPlayerAction.CLAIM_ROUTE.action}\", "route": {"city1": "Roma", "city2": "Venezia"}}
    """.stripMargin)
    aiPlayer.nextAction.onComplete:
      case Success(action) =>
        action._1 should be(AIPlayerAction.CLAIM_ROUTE)
        action._2 should be(Some(("Roma", "Venezia")))
      case _ => fail(ErrorMessage)

  it should "fallback on unparsable response and return draw cards" in:
    when(mockModel.chat(anyString())).thenReturn("invalid response")
    aiPlayer.nextAction.onComplete:
      case Success(action) => action._1 should be(AIPlayerAction.DRAW_CARDS)
      case _ => fail(ErrorMessage)

  it should "fallback when LLM returns a non-existent route and return draw cards" in:
    when(mockModel.chat(anyString())).thenReturn(s"""
      |{"action": \"${AIPlayerAction.CLAIM_ROUTE.action}\", "route": {"city1": "Roma", "city2": "Palermo"}}
    """.stripMargin)
    aiPlayer.nextAction.onComplete:
      case Success(action) => action._1 should be(AIPlayerAction.DRAW_CARDS)
      case _ => fail(ErrorMessage)

  it should "fallback when LLM returns an already occupied route and return draw cards" in:
    when(mockModel.chat(anyString())).thenReturn(s"""
      |{"action": \"${AIPlayerAction.CLAIM_ROUTE.action}\", "route": {"city1": "Roma", "city2": "Venezia"}}
    """.stripMargin)
    gameMap.claimRoute(("Roma", "Venezia"), PlayerId)
    aiPlayer.nextAction.onComplete:
      case Success(action) => action._1 should be(AIPlayerAction.DRAW_CARDS)
      case _ => fail(ErrorMessage)
