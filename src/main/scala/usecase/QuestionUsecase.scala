package usecase

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior

object QuestionUsecase {
  sealed trait Command
  final case class Hoge() extends Command

  def apply(): Behavior[Command] = {
    registry()
  }

  private def registry(): Behavior[Command] = {
    Behaviors.receiveMessage {
      case Hoge() => {
        Behaviors.same
      }
    }
  }
}
