package controller

import controller.parser.AIPlayerResponseParser
import controller.parser.AIPlayerResponse
import controller.prompt.AIPlayerPrompt
import dev.langchain4j.model.chat.ChatModel
import model.map.GameMap
import model.player.Player
import view.GameView.City

/** The actions that an AI player can make when asked for its next action.
  * @param action
  *   the string representation of the action.
  */
enum AIPlayerAction(val action: String):
  case DRAW_CARDS extends AIPlayerAction("draw")
  case CLAIM_ROUTE extends AIPlayerAction("claim")

/** A player controlled by the AI, able to determine the player's next action based on the current state of the player
  * and the game map.
  */
trait AIPlayer:
  /** Computes the next action the AI intends to perform.
    *
    * @return
    *   a tuple where the first element is the [[AIPlayerAction]] taken and the second is optionally a pair of [[City]]
    *   representing the route to claim when the decision is [[AIPlayerAction.CLAIM_ROUTE]].
    */
  def nextAction: (AIPlayerAction, Option[(City, City)])

/** The factory for [[AIPlayer]] instances. */
object AIPlayer:
  /** Creates an [[AIPlayer]] based on a chat model.
    *
    * @param chatModel
    *   the chat model used to query the AI.
    * @param player
    *   the player controlled by the AI.
    * @param gameMap
    *   the game map.
    * @return
    *   the [[AIPlayer]] created.
    */
  def apply(chatModel: ChatModel, player: Player, gameMap: GameMap): AIPlayer =
    AIPlayerImpl(chatModel, player, gameMap)

  private case class AIPlayerImpl(chatModel: ChatModel, player: Player, gameMap: GameMap) extends AIPlayer:
    private val parser = AIPlayerResponseParser()
    private val prompt = AIPlayerPrompt()

    override def nextAction: (AIPlayerAction, Option[(City, City)]) =
      val promptStr = prompt.toPromptString(player, getUnclaimedRoutes)
      parser.parse(chatModel.chat(promptStr)) match
        case Right(AIPlayerResponse.ClaimRoute(city1, city2)) =>
          (AIPlayerAction.CLAIM_ROUTE, Some((city1, city2)))
        case _ =>
          (AIPlayerAction.DRAW_CARDS, None)

    private def getUnclaimedRoutes: Set[(City, City)] = gameMap.routes
      .filter(route =>
        gameMap.getPlayerClaimingRoute((route.connectedCities._1.name, route.connectedCities._2.name))
          .exists(_.isEmpty)
      )
      .map(route => (route.connectedCities._1.name, route.connectedCities._2.name))
