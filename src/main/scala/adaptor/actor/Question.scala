package adaptor.actor

import adaptor.actor.AnswerActor.GetAnswerResponse
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import domain.Answer

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}

object QuestionActor {
  sealed trait Command
  case class AddAnswer(answer: Answer) extends Command
  case class GetAllAnswers() extends Command

  private case class AdaptedResponse(getAnswerResponse: GetAnswerResponse) extends Command

  def apply(): Behavior[Command] = {
    create(Vector.empty);
  }

  private def create(answerActors: Vector[ActorRef[AnswerActor.Command]]): Behavior[Command] = {
    Behaviors.receive { (ctx, msg) =>
      msg match {
        case AddAnswer(answer) =>
          val newAnswerActor = ctx.spawn(AnswerActor(answer), s"answer-${answer.id}")
          create(answerActors :+ newAnswerActor)
        case GetAllAnswers() =>
          implicit val timeout: Timeout = 3.seconds

          answerActors.map(answerActor => ctx.ask(answerActor, AnswerActor.GetAnswer.apply) {
            case Success(v1) =>
              AdaptedResponse(v1)
            case Failure(exception) =>
              AdaptedResponse(null)
          })

          Behaviors.same
      }
    }
  }
}
