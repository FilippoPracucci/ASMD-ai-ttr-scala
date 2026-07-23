package controller

import controller.AIPlayersTurnManager.Action
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.request.ResponseFormat
import dev.langchain4j.model.ollama.OllamaChatModel
import model.objective.{ObjectiveWithCompletion, ObjectivesLoader}
import model.player.Player
import view.GameView.City

/** Trait that represents the controller of the game. */
trait GameController extends DrawCardsController with ClaimRouteController:
  /** Show the rules of the game. */
  def showRules(): Unit

  /** Executes the given action decided by the AI.
    *
    * @param action
    *   the action to execute.
    */
  def executeAIAction(action: Action): Unit

/** Companion object for [[GameController]]. */
object GameController:
  /** Returns the singleton instance of [[GameController]].
    *
    * @return
    *   the globally shared [[GameController]] instance
    */
  def apply(): GameController = GameControllerImpl

  private object ImportHelper:
    export config.ModelConfig.*
    export model.map.GameMap
    export GameMap.defaultRoutesLoader
    export model.utils.PlayerColor
    export model.cards.Deck
    export model.player.Player
    export model.objective.{ObjectiveWithCompletion, ObjectivesLoader}

  private object GameControllerImpl extends GameController:
    import ImportHelper.*
    import ImportHelper.given
    import java.time.Duration

    private val gameMap = GameMap()
    private val deck: Deck = Deck()
    deck.shuffle()

    private val chatModel: ChatModel = OllamaChatModel.builder()
      .baseUrl(BaseUrl)
      .modelName(ModelName)
      .temperature(Temperature)
      .think(false)
      .timeout(Duration.ofMinutes(TimeoutMinutes))
      .responseFormat(ResponseFormat.JSON)
      .build()
    private val players: List[Player] = initPlayers()
    private val aiPlayers: List[AIPlayer] = assignAIPlayers()
    private val turnManager: AIPlayersTurnManager = AIPlayersTurnManager(aiPlayers, TurnManager(players), this)

    private val viewController = ViewController(turnManager, players)

    private val drawCardsController = DrawCardsController(turnManager, viewController)
    private val claimRouteController = ClaimRouteController(turnManager, viewController, gameMap)

    viewController.initGameView(gameMap)

    override def drawCards(): Unit =
      import model.utils.GameError
      drawCardsController.drawCards() match
        case error: GameError => aiPlayers.find(_.player.id == turnManager.currentPlayer.id) match
            case Some(aiPlayer) => aiPlayer.retryAction()
            case None => viewController.reportError(error)
        case _ => ()

    def executeAIAction(action: Action): Unit = action match
      case (AIPlayerAction.DRAW_CARDS, _) => viewController.executeAction(drawCards())
      case (AIPlayerAction.CLAIM_ROUTE, Some((city1, city2))) =>
        viewController.executeAction(claimRoute((city1, city2)))
      case _ => println(s"Invalid action: $action")

    private def initPlayers(): List[Player] =
      import scala.util.Random
      var objectives = ObjectivesLoader().load()
      var objToAssign: Option[ObjectiveWithCompletion] = Option.empty
      var playerList: List[Player] = List.empty
      for
        color <- PlayerColor.values
      yield
        objToAssign = Option(objectives.toList(Random.nextInt(objectives.size)))
        objectives = objectives.excl(objToAssign.get)
        playerList +:= Player(color, deck, objective = objToAssign.get)
      playerList

    private def assignAIPlayers(): List[AIPlayer] =
      var aiPlayersList: List[AIPlayer] = List.empty
      for
        player <- players
        if player.id != players.head.id
      yield aiPlayersList +:= AIPlayer(chatModel, player, gameMap)
      aiPlayersList

    export claimRouteController.claimRoute
    export viewController.{showRules, executeAction}
    export turnManager.retryAction
