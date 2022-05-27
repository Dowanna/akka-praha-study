package adaptor.actor

import adaptor.actor.AnswerActor.GetAnswerResponse
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import domain.Answer

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

  def apply(): Behavior[Command] = {
    create(Vector.empty, Vector.empty, None, Vector.empty, Vector.empty);
  }

  private def create(
      answerActors: Vector[ActorRef[AnswerActor.Command]],
      answers: Vector[Answer],
      replyTo: Option[ActorRef[GetAllAnswersResponse]],
      pendingGetAnswerMessages: Vector[String],
      getAllAnswersQueue: Vector[Answer]
  ): Behavior[Command] = {
    Behaviors.receive { (ctx, msg) =>
      msg match {
        case AddAnswer(answer) =>
          val newAnswerActor = ctx.spawn(AnswerActor(answer), s"answer-${answer.id}")
          create(answerActors :+ newAnswerActor, answers :+ answer, replyTo, pendingGetAnswerMessages, getAllAnswersQueue)
        case GetAllAnswers(replyTo) =>
          implicit val timeout: Timeout = 3.seconds
          val pendingAnswers = answers.map(answer => answer.id)

          answerActors.map(answerActor =>
            ctx.ask(answerActor, AnswerActor.GetAnswer.apply) {
              case Success(v1) =>
                AdaptedResponse(v1)
              case Failure(exception) =>
                AdaptedResponse(null)
            }
          )
          create(answerActors, answers, Some(replyTo), pendingAnswers, getAllAnswersQueue)
        case AdaptedResponse(getAnswerResponse) => {
          getAnswerResponse match {
            case null =>
              Behaviors.same
            case answer =>
              val stillPending = pendingGetAnswerMessages.filter(id => id != answer.answer.id)
              stillPending.length == 0 match {
                case true =>
                  ctx.self ! FinishedGetAllAnswers()
                case false =>
              }
              create(answerActors, answers, replyTo, stillPending, getAllAnswersQueue :+ answer.answer)
          }
        }
        case FinishedGetAllAnswers() => {
          replyTo match {
            case Some(replyTo) =>
              replyTo ! GetAllAnswersResponse(getAllAnswersQueue)
            case None =>
          }
          create(answerActors, answers, replyTo, pendingGetAnswerMessages, Vector.empty)
        }
      }
    }
  }
}
