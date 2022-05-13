package typed // praha-incとprahaincの違いはどう吸収すれば良いのだろう

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior

object Supervisor {
  sealed trait Command
  final case object RandomCommand extends Command
  def apply(): Behavior[Command] = Behaviors.setup[Command](context => {
    // context.log.info("behavior.setup complete!")
    Behaviors.receiveMessage[Command] { _ =>
      //   context.log.info("received message!")
      Behaviors.same
    }
  })
}

object PrahaStudy {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem[Supervisor.Command](Supervisor(), "akka-praha-study")
    system ! Supervisor.RandomCommand
  }
}
