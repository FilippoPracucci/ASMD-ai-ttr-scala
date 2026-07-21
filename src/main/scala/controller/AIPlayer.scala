package controller

import controller.parser.AIPlayerResponseParser
import controller.parser.AIPlayerResponse
import controller.prompt.AIPlayerPrompt
import dev.langchain4j.model.chat.ChatModel
import model.map.GameMap
import model.player.Player
import view.GameView.City

import scala.concurrent.{ExecutionContext, Future}

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
  /** The player controlled by the AI.
    *
    * @return
    *   the player controlled by the AI.
    */
  def player: Player

  /** Computes the next action the AI intends to perform.
    *
    * @param onlyClaim
    *   [[true]] if the player can only claim routes, [[false]] otherwise.
    * @return
    *   a tuple where the first element is the [[AIPlayerAction]] taken and the second is optionally a pair of [[City]]
    *   representing the route to claim when the decision is [[AIPlayerAction.CLAIM_ROUTE]].
    */
  def nextAction(onlyClaim: Boolean = false): Future[(AIPlayerAction, Option[(City, City)])]

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

  private case class AIPlayerImpl(chatModel: ChatModel, override val player: Player, gameMap: GameMap) extends AIPlayer:
    import model.utils.Color
    import model.map.Route.SpecificColor

    private val MaxRetries = 3

    private val parser = AIPlayerResponseParser()
    private val prompt = AIPlayerPrompt()

    override def nextAction(onlyClaim: Boolean): Future[(AIPlayerAction, Option[(City, City)])] =
      import scala.util.Try
      import scala.annotation.tailrec
      import ExecutionContext.Implicits.global

      @tailrec
      def attempt(remainingRetries: Int): (AIPlayerAction, Option[(City, City)]) =
        val promptStr = if onlyClaim then
          prompt.toOnlyClaimPromptString(player, unclaimedRoutes, ownedRoutes)
        else
          prompt.toStandardPromptString(player, unclaimedRoutes, ownedRoutes)
        Try(parser.parse(chatModel.chat(promptStr))).toOption.flatMap(_.toOption) match
          case Some(AIPlayerResponse.ClaimRoute(city1, city2)) if (city1, city2).isValid =>
            (AIPlayerAction.CLAIM_ROUTE, Some((city1, city2)))
          case Some(AIPlayerResponse.DrawCards) => (AIPlayerAction.DRAW_CARDS, None)
          case _ => if remainingRetries > 0 then attempt(remainingRetries - 1) else (AIPlayerAction.DRAW_CARDS, None)

      Future(attempt(MaxRetries))

    private def unclaimedRoutes: Set[((City, City), Color, Int)] = gameMap.routes.filter(route =>
      gameMap
        .getPlayerClaimingRoute((route.connectedCities._1.name, route.connectedCities._2.name))
        .exists(_.isEmpty)
    ).collect({ case route =>
      (
        (route.connectedCities._1.name, route.connectedCities._2.name),
        route.mechanic match
          case SpecificColor(color) => color,
        route.length
      )
    })

    private def ownedRoutes: Set[(City, City)] = gameMap.routes.filter(route =>
      gameMap
        .getPlayerClaimingRoute((route.connectedCities._1.name, route.connectedCities._2.name))
        .exists(_ == player.id)
    ).collect({ case route => (route.connectedCities._1.name, route.connectedCities._2.name) })

    extension (route: (City, City))
      private def isValid: Boolean =
        unclaimedRoutes.collect(_._1).intersect(Set((route._1, route._2), (route._2, route._1))).nonEmpty &&
          gameMap.getRoute((route._1, route._2)).exists(route =>
            route.mechanic match
              case SpecificColor(color) =>
                player.canPlayCards(color, route.length) && player.canPlaceTrains(route.length)
              case _ => false
          )
