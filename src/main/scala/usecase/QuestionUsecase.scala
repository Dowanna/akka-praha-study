package usecase

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior
import route.QuestionRoutes._
import domain.{Question, Tag}

object QuestionUsecase {
  sealed trait Command
  final case class Create(questionRequest: QuestionRequest) extends Command

  def apply(): Behavior[Command] = {
    registry()
  }

  private def registry(): Behavior[Command] = {
    Behaviors.receiveMessage {
      case Create(questionRequest) => {
        //        val question = Question(id: questionRequest.id)
        Question("id", "title", "body", Set.empty, Set(Tag("test"))) match {
          case Left(_) => None
          case Right(question) => Some(
            QuestionResponse(
              id = question.id,
              title = question.title,
              body = question.body,
              answers = question.answers.map(answer => AnswerResponse(id = answer.id, text = answer.text, tags = answer.tags.map(tag => TagResponse(tag.name)))),
              tags = question.tags.map(tag => TagResponse(tag.name))))
        }
        Behaviors.same
      }
    }
  }
}
