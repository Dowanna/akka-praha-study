package usecase

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior
import route.QuestionRoutes._
import domain.{Question, Tag}
import akka.actor.typed.ActorRef

object QuestionUsecase {
  sealed trait Command
  final case class Create(questionRequest: QuestionRequest, replyTo: ActorRef[Response]) extends Command

  final case class Get(id: String, replyTo: ActorRef[Response]) extends Command

  sealed trait Response
  final case class SuccessResponse(questionResponse: QuestionResponse) extends Response
  final case class FailedResponse() extends Response

  def apply(): Behavior[Command] = {
    registry()
  }

  private def registry(): Behavior[Command] = {
    Behaviors.receiveMessage {
      case Create(questionRequest, replyTo) => {
        Question(
          id = questionRequest.id,
          title = questionRequest.title,
          body = questionRequest.body,
          Set.empty,
          tags = questionRequest.tags.fold(Set.empty[Tag])(tagRequestSet => tagRequestSet.map(tagRequest => Tag(tagRequest.name)))
        ) match {
          case Left(_) => replyTo ! FailedResponse()
          case Right(question) =>
            replyTo ! SuccessResponse(QuestionResponse(
              id = question.id,
              title = question.title,
              body = question.body,
              answers = question.answers.map(answer => AnswerResponse(id = answer.id, text = answer.text, tags = answer.tags.map(tag => TagResponse(tag.name)))),
              tags = question.tags.map(tag => TagResponse(tag.name))))
        }
        Behaviors.same
      }

      case Get(id) => {

      }
    }
  }
}
