package adaptor.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object QuestionActor {
  sealed trait Command
  def apply(): Behavior[Command] = {
    Behaviors.receive { (ctx, msg) =>
      Behaviors.same
    }
  }
}
