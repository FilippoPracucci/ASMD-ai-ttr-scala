package controller.prompt

import model.player.Player
import model.utils.Color
import view.GameView.City

/** Provides prompts for AI players as formatted strings.
  */
trait AIPlayerPrompt:
  /** Creates a prompt string for the AI based on the player and available routes.
    * @param player
    *   the player controlled by the AI.
    * @param unclaimedRoutes
    *   the set of available routes, as tuples of a pair of city names, color and length.
    * @param ownedRoutes
    *   the set of routes already owned by the player.
    * @return
    *   a formatted prompt string.
    */
  def toStandardPromptString(player: Player, unclaimedRoutes: Set[((City, City), Color, Int)],
      ownedRoutes: Set[(City, City)]): String

  /** Creates a prompt string for the AI when it can only claim a route.
    * @param player
    *   the player controlled by the AI.
    * @param unclaimedRoutes
    *   the set of available routes, as tuples of a pair of city names, color and length.
    * @param ownedRoutes
    *   the set of routes already owned by the player.
    * @return
    *   a formatted prompt string.
    */
  def toOnlyClaimPromptString(player: Player, unclaimedRoutes: Set[((City, City), Color, Int)],
      ownedRoutes: Set[(City, City)]): String

/** The factory for [[AIPlayerPrompt]] instances. */
object AIPlayerPrompt:
  private val template =
    s"""You are a player in a game of Ticket to Ride.
        |The board consists of a map of cities connected by routes, each route having a color and a length.
        |Your goal is to gain more points than your opponents by claiming routes and completing the objective.
        |Claiming a route gives you points based on the length of the route, following the schema (length -> points):
        |1 -> 1, 2 -> 2, 3 -> 4, 4 -> 7, 6 -> 15, 8 -> 21.
        |You can either draw cards or claim a route on your turn.
        |To claim a route, you need to have the required number of cards of the route's color in your hand.
        |When selecting a route to claim give priority to the routes that are part of the path connecting the two cities
        |in your objective.
        |""".stripMargin

  private val prompt = (citiesToConnect: (String, String), points: Int, hand: String, routes: String,
      ownedRoutes: String) =>
    template.concat(
      s"""
          |Your objective is to connect ${citiesToConnect._1} and ${citiesToConnect._2}, owing a sequence of routes that
          |consists in a path between them, to gain $points points.
          |You have the following cards in your hand: $hand.
          |The routes, represented as [(city1, city2), color, length], not occupied are: $routes.
          |The routes you own are: $ownedRoutes.
          |Take a decision based on your current situation and trying to accumulate points in time.
          |Respond ONLY with a JSON object:
          |{"action": "draw"} or {"action": "claim", "route": {"city1": <c1>, "city2": <c2>}}.
          |No explanation.
          |""".stripMargin
    )

  private val onlyClaimPrompt = (citiesToConnect: (String, String), points: Int, hand: String, routes: String,
      ownedRoutes: String) =>
    template.concat(
      s"""
          |Your objective is to connect ${citiesToConnect._1} and ${citiesToConnect._2}, owing a sequence of routes that
          |consists in a path between them, to gain $points points.
          |You have the following cards in your hand: $hand.
          |The routes, represented as [(city1, city2), color, length], not occupied are: $routes.
          |The routes you own are: $ownedRoutes.
          |Chose a route to claim based on your current situation and trying to accumulate points in time.
          |Respond ONLY with a JSON object: {"action": "claim", "route": {"city1": <c1>, "city2": <c2>}}.
          |No explanation.
          |""".stripMargin
    )

  /** Creates a [[AIPlayerPrompt]].
    *
    * @return
    *   a [[AIPlayerPrompt]] created.
    */
  def apply(): AIPlayerPrompt = AIPlayerPromptImpl()

  private case class AIPlayerPromptImpl() extends AIPlayerPrompt:
    override def toStandardPromptString(player: Player, unclaimedRoutes: Set[((City, City), Color, Int)],
        ownedRoutes: Set[(City, City)]): String =
      prompt(
        player.objective.citiesToConnect,
        player.objective.points,
        player.hand.map(_.color.toString).mkString(", "),
        unclaimedRoutesFormat(unclaimedRoutes),
        ownedRoutesFormat(ownedRoutes)
      )

    override def toOnlyClaimPromptString(player: Player, unclaimedRoutes: Set[((City, City), Color, Int)],
        ownedRoutes: Set[(City, City)]): String =
      onlyClaimPrompt(
        player.objective.citiesToConnect,
        player.objective.points,
        player.hand.map(_.color.toString).mkString(", "),
        unclaimedRoutesFormat(unclaimedRoutes),
        ownedRoutesFormat(ownedRoutes)
      )

    private def unclaimedRoutesFormat(unclaimedRoutes: Set[((City, City), Color, Int)]): String =
      unclaimedRoutes.routesToStringFormat(route => s"([(${route._1._1}, ${route._1._2}), ${route._2}, ${route._3}]")

    private def ownedRoutesFormat(ownedRoutes: Set[(City, City)]): String =
      ownedRoutes.routesToStringFormat(route => s"(${route._1}, ${route._2})")

    extension [A](routes: Set[A])
      private def routesToStringFormat(render: A => String): String = routes.map(render).mkString(", ")
