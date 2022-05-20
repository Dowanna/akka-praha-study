package adaptor.actor

import adaptor.actor.AnswerActor.GetAnswerResponse
import akka.actor.Status.Success
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import domain.Answer

import scala.concurrent.duration.DurationInt

object QuestionActor {
  sealed trait Command
  case class AddAnswer(answer: Answer) extends Command
  case class GetAllAnswers() extends Command

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
          answerActors.map(answerActor => ctx.ask(answerActor, AnswerActor.GetAnswer.apply)({
//            case Success(value) => "hoge"
//            case Failure(exception) => "hoge"
          }))
          Behaviors.same
      }
    }
  }
}
