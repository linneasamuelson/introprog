package snake

object SnakeGame:
  enum State:
    case Starting, Playing, GameOver, Quitting
  export State.*
  
abstract class SnakeGame(settings: Settings) extends introprog.BlockGame(
  title                 = settings.windowTitle,
  dim                   = settings.windowSize,
  blockSize             = settings.blockSize,
  background            = settings.background,
  framesPerSecond       = settings.framesPerSecond,
  messageAreaHeight     = settings.messageAreaHeight,
  messageAreaBackground = settings.messageAreaBackground
):
  // exempel på olika synlighet (diskutera val av synlighet utifrån användning)
  var entities: Vector[Entity] = Vector.empty
  protected var players: Vector[Player] = Vector.empty
  private var isPaused = false

  import SnakeGame.*  

  protected var state: State = Starting
  private var _iterationsSinceStart = 0
  def iterationsSinceStart = _iterationsSinceStart

  def enterStartingState(): Unit =
    pixelWindow.clear()
    pixelWindow.drawText("tryck space för start", 20, 30, Colors.Pink, 13)
  //sudda, meddela "tryck space för start"

  def enterPlayingState(): Unit =
    pixelWindow.clear()
    entities.foreach(_.reset())
    entities.foreach(_.draw())
    state = Playing
  //sudda, för varje entitet: nollställ & rita

  def enterGameOverState(): Unit =
    pixelWindow.drawText("game over", 20, 30, Colors.Pink, 13)
    state = GameOver

  def enterQuittingState(): Unit = 
    println("Goodbye!")
    pixelWindow.hide()
    state = Quitting

  def randomFreePos(): Pos = 
    var pos = Pos.random(Dim(dim))
    while entities.exists(_.isOccupyingBlockAt(pos)) do
      pos =  Pos.random(Dim(dim))

    pos
 // dra slump-pos tills ledig plats, används av frukt, monster

  override def onKeyDown(key: String): Unit = 
    println(s"""key "$key" pressed""")
    state match 
      case Starting => if key == " " then enterPlayingState()

      case Playing =>
        if key == "Escape" then
          println(s"Toggle pause: isPaused == $isPaused")
          isPaused = !isPaused
        else
          players.foreach(_.handleKey(key))

      case GameOver =>
        if key == " " then enterPlayingState()
        else if key == "Escape" then enterQuittingState()

      case _ =>

  override def onClose(): Unit = 
    println("Window Closed!")
    enterQuittingState()

  /** Implement this with logic for when to end the game */ 
  def isGameOver: Boolean

  /** Override this if you want to add game-logic in gameLoopAction
   *  Call super.onIteration() if you want to keep the step counter.
   */
  def onIteration(): Unit = 
    clearMessageArea()
    drawTextInMessageArea(s"Number of steps: $iterationsSinceStart", 10, 2)

  override def gameLoopAction(): Unit = 
    if state == Playing && !isPaused then
      _iterationsSinceStart += 1
      entities.foreach(_.erase())
      entities.foreach(_.update())
      entities.foreach(_.draw())
      onIteration()
      if isGameOver then enterGameOverState()

  final def start(ps: Player*)(es: Entity*): Unit = 
    players = ps.toVector
    entities = es.toVector
    isPaused = false
    pixelWindow.show()  // möjliggör omstart även om fönstret stängts...
    enterStartingState()
    gameLoop(stopWhen = state == Quitting)

  /** Implement this with a call to start with specific players and entities. */
  def play(): Unit

