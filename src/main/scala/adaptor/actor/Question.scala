package adaptor.actor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import domain.Answer

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
          answerActors.map(answerActor => answerActor ! AnswerActor.GetAnswer())
      }
    }
  }
}
