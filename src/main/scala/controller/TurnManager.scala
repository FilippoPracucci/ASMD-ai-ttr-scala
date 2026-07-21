package controller

import model.player.Player

/** The different game states. */
enum GameState:
  case IN_GAME, START_LAST_ROUND, LAST_ROUND, END_GAME

/** A players' turn manager, which has the list of players, the current player playing and can switch players turn. */
trait TurnManager:
  /** The current player on turn.
    *
    * @return
    *   the player on turn.
    */
  def currentPlayer: Player

  /** Returns whether the current player is controlled by an AI.
    *
    * @return
    *   [[true]] when the current player is controlled by the AI, [[false]] otherwise.
    */
  def currentPlayerIsAI: Boolean

  /** Switch players' turn following the order. */
  def switchTurn(): Unit

  /** The actual game state.
    *
    * @return
    *   the actual game state.
    */
  def gameState: GameState

/** The factory for [[TurnManager]] instances. */
object TurnManager:
  /** Create a [[TurnManager]], which manages the given list of players and AI players.
    *
    * @param players
    *   the list of players.
    * @param aiPlayers
    *   the list of AI players.
    * @return
    *   the turn manager created.
    */
  def apply(players: List[Player], aiPlayers: List[AIPlayer]): TurnManager = TurnManagerImpl(players, aiPlayers)

  private class TurnManagerImpl(players: List[Player], aiPlayers: List[AIPlayer]) extends TurnManager:
    import controller.GameState.*

    private var _currentPlayer: Player = players.head
    private var _gameState: GameState = IN_GAME
    private var _playerStartedLastRound: Option[Player] = Option.empty
    private val gameController = GameController()

    override def currentPlayer: Player = _currentPlayer
    private def currentPlayer_=(player: Player): Unit = _currentPlayer = player

    override def currentPlayerIsAI: Boolean = aiPlayers.exists(_.player.id == currentPlayer.id)

    override def gameState: GameState = _gameState

    override def switchTurn(): Unit =
      updateGameState()
      currentPlayer = players((players.indexOf(currentPlayer) + 1) % players.size)
      gameController.aiPlayerAct()

    private def updateGameState(): Unit =
      import config.GameConfig.TrainsToStartLastRound
      _playerStartedLastRound match
        case Some(player) => _gameState = if currentPlayer == player then END_GAME else LAST_ROUND
        case None if currentPlayer.trains <= TrainsToStartLastRound =>
          _gameState = START_LAST_ROUND
          _playerStartedLastRound = Option(currentPlayer)
        case _ => _gameState = IN_GAME
