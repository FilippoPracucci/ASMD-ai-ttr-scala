package controller

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.request.ResponseFormat
import dev.langchain4j.model.ollama.OllamaChatModel
import model.objective.{ObjectiveWithCompletion, ObjectivesLoader}
import model.player.Player
import view.GameView.City

import scala.concurrent.Future

/** Trait that represents the controller of the game. */
trait GameController extends DrawCardsController with ClaimRouteController:
  /** Show the rules of the game. */
  def showRules(): Unit

  /** AI player performs an action, which can be either drawing cards or claiming a route. */
  def aiPlayerAct(): Unit

  /** The given player controlled by the AI retries the action.
    *
    * @param player
    *   the [[Player]] for whom the action should be retried.
    */
  def retryAiPlayerAction(player: Player): Unit

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
    private val turnManager: TurnManager = TurnManager(players, aiPlayers)

    private val viewController = ViewController(turnManager, players, aiPlayers)

    private val drawCardsController = DrawCardsController(turnManager, viewController)
    private val claimRouteController = ClaimRouteController(turnManager, viewController, gameMap)

    viewController.initGameView(gameMap)

    override def aiPlayerAct(): Unit = aiPlayers.find(_.player.id == turnManager.currentPlayer.id).foreach: aiPlayer =>
      aiPlayer.nextAction().onCompletePlayerAction(aiPlayer)

    override def retryAiPlayerAction(player: Player): Unit = aiPlayers.find(_.player.id == player.id).foreach: aiPlayer =>
      aiPlayer.nextAction(onlyClaim = true).onCompletePlayerAction(aiPlayer)

    extension (action: Future[(AIPlayerAction, Option[(City, City)])])
      private def onCompletePlayerAction(aiPlayer: AIPlayer): Unit =
        import scala.util.Success
        import scala.concurrent.ExecutionContext.Implicits.global
        action.onComplete {
          case Success((AIPlayerAction.DRAW_CARDS, _)) => viewController.executeAction(drawCards())
          case Success((AIPlayerAction.CLAIM_ROUTE, Some((city1, city2)))) =>
            viewController.executeAction(claimRouteController.claimRoute((city1, city2)))
          case _ => println(s"AIPlayer ${aiPlayer.player.id} returned an invalid action, defaulting to drawing cards.")
        }

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

    override def drawCards(): Unit =
      import model.utils.GameError
      drawCardsController.drawCards() match
        case _: GameError => retryAiPlayerAction(turnManager.currentPlayer)
        case _ => ()

    export claimRouteController.claimRoute
    export viewController.showRules
