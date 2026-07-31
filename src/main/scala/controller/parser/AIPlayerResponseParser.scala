package controller.parser

import controller.AIPlayerAction
import view.GameView.City

/** The responses that can be returned by an AI player. */
sealed trait AIPlayerResponse

object AIPlayerResponse:
  /** The response indicating the AI decided to draw cards. */
  case object DrawCards extends AIPlayerResponse

  /** The response indicating the AI decided to claim a route between two cities.
    *
    * @param city1
    *   the name of the first city.
    * @param city2
    *   the name of the second city.
    */
  case class ClaimRoute(city1: City, city2: City) extends AIPlayerResponse

/** The parser from the raw JSON response produced by an LLM to a [[AIPlayerResponse]]. */
trait AIPlayerResponseParser:
  /** Try to parse the given JSON string into an [[AIPlayerResponse]].
    *
    * @param json
    *   the raw JSON string.
    * @return
    *   the [[AIPlayerResponse]] if parsing succeeds, otherwise an [[IllegalArgumentException]].
    */
  def parse(json: String): Either[IllegalArgumentException, AIPlayerResponse]

/** The factory for [[AIPlayerResponseParser]] instances.
  */
object AIPlayerResponseParser:
  /** Creates an [[AIPlayerResponseParser]] exploiting ujson.
    *
    * @return
    *   a ujson-based parser.
    */
  def apply(): AIPlayerResponseParser = UjsonAIPlayerResponseParser()

  private case class UjsonAIPlayerResponseParser() extends AIPlayerResponseParser:
    override def parse(json: String): Either[IllegalArgumentException, AIPlayerResponse] = ujson.read(json).objOpt match
      case Some(value) => value("action").str match
          case AIPlayerAction.CLAIM_ROUTE.action =>
            val route = value("route").obj
            Right(AIPlayerResponse.ClaimRoute(route("city1").str, route("city2").str))
          case _ => Right(AIPlayerResponse.DrawCards)
      case _ => Left(new IllegalArgumentException("Invalid JSON format"))
