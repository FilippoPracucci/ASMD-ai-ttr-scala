package controller

import model.utils.GameError

/** The controller about drawing cards from the deck. */
trait DrawCardsController:
  /** Player action that consists in drawing cards from the deck.
    *
    * @return
    *   a [[GameError]] if the action does not succeed and the current player is controlled by the AI.
    */
  def drawCards(): GameError | Unit

object DrawCardsController:
  /** Creates a [[DrawCardsController]].
    *
    * @param turnManager
    *   the turn manager.
    * @param viewController
    *   the controller of the view.
    * @return
    *   the created [[DrawCardsController]].
    */
  def apply(turnManager: AIPlayersTurnManager, viewController: ViewController): DrawCardsController =
    DrawCardsControllerImpl(turnManager, viewController)

  private class DrawCardsControllerImpl(turnManager: AIPlayersTurnManager, viewController: ViewController)
      extends DrawCardsController:

    override def drawCards(): GameError | Unit =
      import config.GameConfig.StandardNumberOfCardsToDraw
      turnManager.currentPlayer.drawCards(StandardNumberOfCardsToDraw) match
        case Right(_) =>
          turnManager.switchTurn()
          viewController.updateViewNewTurn()
        case Left(gameError) => gameError
