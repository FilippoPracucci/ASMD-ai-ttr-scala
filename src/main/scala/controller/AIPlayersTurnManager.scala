package controller

import view.GameView.City

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/** An AI players turn manager, which handles their turn and the execution of their actions. */
trait AIPlayersTurnManager extends TurnManager:
  /** Returns whether the current player is controlled by an AI.
    *
    * @return
    *   [[true]] when the current player is controlled by the AI, [[false]] otherwise.
    */
  def currentPlayerIsAI: Boolean

  /** Executes the turn of the current player if it is controlled by an AI. */
  def aiPlayerTurn(): Unit

  extension (aiPlayer: AIPlayer)
    /** Executes the next action of the AI player. */
    def act(): Unit

    /** Retries executing the next action of the AI player. */
    def retryAction(): Unit

/** The factory for [[AIPlayersTurnManager]] instances. */
object AIPlayersTurnManager:
  /** Type alias that represents AI player's action as tuple of [[AIPlayerAction]] and [[Option]] of a pair of [[City]]
    */
  type Action = (AIPlayerAction, Option[(City, City)])

  def apply(aiPlayers: List[AIPlayer], turnManager: TurnManager, gameController: GameController): AIPlayersTurnManager =
    AIPlayersTurnManagerImpl(aiPlayers, turnManager, gameController)

  private class AIPlayersTurnManagerImpl(aiPlayers: List[AIPlayer], turnManager: TurnManager,
      gameController: GameController) extends AIPlayersTurnManager:
    override def currentPlayerIsAI: Boolean = aiPlayers.exists(_.player.id == currentPlayer.id)

    override def aiPlayerTurn(): Unit = aiPlayers.find(_.player.id == currentPlayer.id).foreach(_.act())

    override def switchTurn(): Unit =
      turnManager.switchTurn()
      if currentPlayerIsAI then aiPlayerTurn()

    extension (aiPlayer: AIPlayer)
      override def act(): Unit = aiPlayer.nextAction().onCompletePlayerAction(aiPlayer)

      override def retryAction(): Unit = aiPlayer.nextAction(onlyClaim = true).onCompletePlayerAction(aiPlayer)

    extension (action: Future[Action])
      private def onCompletePlayerAction(aiPlayer: AIPlayer): Unit =
        action.onComplete(a => gameController.executeAIAction(a.get))

    export turnManager.{currentPlayer, gameState}
