package controller.prompt

import model.player.Player
import view.GameView.City

/**
  * Provides prompts for AI players as formatted strings.
  */
trait AIPlayerPrompt:
  /**
   * Creates a prompt string for the AI based on the player and available routes.
   * @param player
   *   the player controlled by the AI.
   * @param routes
   *   the set of available routes, as tuples of city names.
   * @return
   *   a formatted prompt string.
   */
  def toPromptString(player: Player, routes: Set[(City, City)]): String

/** The factory for [[AIPlayerPrompt]] instances. */
object AIPlayerPrompt:
  private val template = (objective: String, hand: String, routes: String) =>
    s"""You are a player in a game of Ticket to Ride. Your objective is: $objective.
      |You have the following cards in your hand: $hand.
      |The routes not occupied are: $routes.
      |Your goal is to complete your objective.
      |You can either draw cards or claim a route on your turn.
      |To claim a route, you need to have the required number of cards of the route's color in your hand.
      |Take a decision based on your current situation and respond ONLY with a JSON object:
      |{"action": "draw"} or {"action": "claim", "route": {"city1": <c1>, "city2": <c2>}}. No explanation.
    """.stripMargin

  /**
    * Creates a [[AIPlayerPrompt]].
    *
    * @return
    *   a [[AIPlayerPrompt]] created.
    */
  def apply(): AIPlayerPrompt = AIPlayerPromptImpl()

  private case class AIPlayerPromptImpl() extends AIPlayerPrompt:
    override def toPromptString(player: Player, routes: Set[(City, City)]): String =
      template(
        player.objective.toString,
        player.hand.foldLeft("")(_ + ", " + _.color.toString),
        routes.foldLeft("")((acc, route) => acc + s"(${route._1}, ${route._2}), ")
      )
