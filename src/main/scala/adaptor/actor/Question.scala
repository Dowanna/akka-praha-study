package adaptor.actor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import domain.Answer

object QuestionActor {
  sealed trait Command
  case class AddAnswer(answer: Answer) extends Command

  def apply(): Behavior[Command] = {
    create(Vector.empty);
  }
  
  private def create(answerActors: Vector[ActorRef[]]) :Behavior[Command] = {
    Behaviors.receive{ (ctx, msg) => 
      msg match {
        case AddAnswer =>
          Behaviors.empty
          // answerActorをspawn
          // spawnしたactorRef+元々のやつを足して、引数として再度createを呼ぶ
      }
    }
  }
}
