package controller

import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock

class AIPlayerTest extends AnyFlatSpec with Matchers with BeforeAndAfterEach:
  import dev.langchain4j.model.chat.ChatModel
  import view.GameView.City as CityName
  import config.Loader
  import model.map.{City, GameMap, Route}
  import model.player.Player
  import model.utils.Color
  import model.objective.{ObjectiveCompletion, ObjectiveWithCompletion}
  import model.utils.PlayerColor

  private val TimeoutMillis = 5000
  private val PlayerId = PlayerColor.GREEN

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

  override def beforeEach(): Unit = gameMap = GameMap()(using loader)

  "An AIPlayer" should "return parsed choice of drawing cards" in:
    when(mockModel.chat(anyString())).thenReturn(s"{\"action\": \"${AIPlayerAction.DRAW_CARDS.action}\"}")
    val aiPlayer = AIPlayer(mockModel, player, gameMap)
    val decision: (AIPlayerAction, _) = aiPlayer.nextAction
    decision._1 should be(AIPlayerAction.DRAW_CARDS)

  it should "return parsed choice of claiming a route" in:
    when(mockModel.chat(anyString())).thenReturn(s"""
      |{"action": \"${AIPlayerAction.CLAIM_ROUTE.action}\", "route": {"city1": "Roma", "city2": "Palermo"}}
    """.stripMargin)
    val aiPlayer = AIPlayer(mockModel, player, gameMap)
    val action: (AIPlayerAction, Option[(CityName, CityName)]) = aiPlayer.nextAction
    action._1 should be(AIPlayerAction.CLAIM_ROUTE)
    action._2 should be(Some(("Roma", "Palermo")))
