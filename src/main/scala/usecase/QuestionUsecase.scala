package usecase

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior
import route.QuestionRoutes._
import domain.{Question, Tag}
import akka.actor.typed.ActorRef

object QuestionUsecase {
  sealed trait Command
  final case class Create(questionRequest: QuestionRequest, replyTo: ActorRef[Response]) extends Command
  final case class Response(questionResponse: QuestionResponse)

  def apply(): Behavior[Command] = {
    registry()
  }

  private def registry(): Behavior[Command] = {
    Behaviors.receiveMessage {
      case Create(replyTo, questionRequest) => {
        //        val question = Question(id: questionRequest.id)
        Question("id", "title", "body", Set.empty, Set(Tag("test"))) match {
          case Left(_) => None
          case Right(question) => Some(
            replyTo ! Response(QuestionResponse(
              id = question.id,
              title = question.title,
              body = question.body,
              answers = question.answers.map(answer => AnswerResponse(id = answer.id, text = answer.text, tags = answer.tags.map(tag => TagResponse(tag.name)))),
              tags = question.tags.map(tag => TagResponse(tag.name)))))
        }
        Behaviors.same
      }
    }
  }
}
