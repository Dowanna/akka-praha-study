package usecase

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior
import route.QuestionRoutes._
import domain.Question

object QuestionUsecase {
  sealed trait Command
  final case class Create(questionRequest: QuestionRequest) extends Command

  def apply(): Behavior[Command] = {
    registry()
  }

  private def registry(): Behavior[Command] = {
    Behaviors.receiveMessage {
      case Create(questionRequest) => {
        val question = Question(id: questionRequest.id)
        Behaviors.same
      }
    }
  }
}
