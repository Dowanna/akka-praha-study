package typed // praha-incとprahaincの違いはどう吸収すれば良いのだろう

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import usecase.QuestionUsecase
import route.QuestionRoutes
import scala.util.Success
import scala.util.Failure

object PrahaStudy {
////#start-http-server
  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start
    import system.executionContext

    val futureBinding = Http().newServerAt("localhost", 8080).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  // #start-http-server
  def main(args: Array[String]): Unit = {
    // #server-bootstrapping
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val questionUsecase = context.spawn(QuestionUsecase(), "QuestionUsecase")
      context.watch(questionUsecase)

      val routes = new QuestionRoutes(context = context, usecase = questionUsecase) // (context.system)
      startHttpServer(routes.questionRoutes)(context.system)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "akka-praha-study")
    // #server-bootstrapping
  }
}

object Supervisor {
  sealed trait Command
  final case object RandomCommand extends Command
  def apply(): Behavior[Command] = Behaviors.setup[Command](context => {
    context.log.info("behavior.setup complete!")
    Behaviors.receiveMessage[Command] { _ =>
      context.log.info("received message!")
      Behaviors.same
    }
  })
}
