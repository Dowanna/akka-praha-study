package adaptor.actor

import adaptor.actor.AnswerActor.GetAnswerResponse
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import domain.{Answer, Question}

import scala.:+
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}

object QuestionActor {
  // このactorが受信するmessage
  sealed trait Command
  case class AddAnswer(answer: Answer) extends Command
  case class GetAllAnswers(replyTo: ActorRef[GetAllAnswersResponse]) extends Command
  case class FinishedGetAllAnswers() extends Command

  // このactorが受信した際に変換するmessage
  private case class AdaptedResponse(getAnswerResponse: GetAnswerResponse) extends Command

  // このactorが送信するmessage
  case class GetAllAnswersResponse(answers: Vector[Answer])

  def apply(question: Question): Behavior[Command] = {
    create(Vector.empty, question, None, Set.empty, Vector.empty);
  }

  private def create(
      answerActors: Vector[ActorRef[AnswerActor.Command]],
      question: Question,
      replyTo: Option[ActorRef[GetAllAnswersResponse]],
      pendingGetAnswerMessages: Set[String],
      getAllAnswersQueue: Vector[Answer]
  ): Behavior[Command] = {
    Behaviors.receive { (ctx, msg) =>
      msg match {
        case AddAnswer(answer) =>
          val newAnswerActor = ctx.spawn(AnswerActor(answer), s"answer-${answer.id}")
          question.addAnswer(answer) match {
            case Left(_) =>
              // todo: もし追加に失敗した時の処理を書く
              Behaviors.same
            case Right(e) =>
              create(answerActors :+ newAnswerActor, question, replyTo, pendingGetAnswerMessages, getAllAnswersQueue)
          }
        case GetAllAnswers(replyTo) =>
          implicit val timeout: Timeout = 3.seconds
          val pendingAnswers = question.answers.map(answer => answer.id)

          answerActors.map(answerActor =>
            ctx.ask(answerActor, AnswerActor.GetAnswer.apply) {
              case Success(v1) =>
                AdaptedResponse(v1)
              case Failure(exception) =>
                AdaptedResponse(null)
            }
          )
          create(answerActors, question, Some(replyTo), pendingAnswers, getAllAnswersQueue)
        case AdaptedResponse(getAnswerResponse) => {
          getAnswerResponse match {
            case null =>
              Behaviors.same
            case answer =>
              val stillPending = pendingGetAnswerMessages.filter(id => id != answer.answer.id)
              stillPending.isEmpty match {
                case true =>
                  ctx.self ! FinishedGetAllAnswers()
                case false =>
              }
              create(answerActors, question, replyTo, stillPending, getAllAnswersQueue :+ answer.answer)
          }
        }
        case FinishedGetAllAnswers() => {
          replyTo match {
            case Some(replyTo) =>
              replyTo ! GetAllAnswersResponse(getAllAnswersQueue)
            case None =>
          }
          create(answerActors, question, replyTo, pendingGetAnswerMessages, Vector.empty)
        }
      }
    }
  }
}
