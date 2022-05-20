package adaptor.actor
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import domain.Answer

object AnswerActor {
  sealed trait Command
  case class GetAnswer(replyTo: ActorRef[GetAnswerResponse]) extends Command
  case class GetAnswerResponse(answer: Answer)
  case class UpdateAnswer() extends Command

  def apply(answer: Answer): Behavior[Command] = {
    hoge(answer)
  }

  private def hoge(answer: Answer): Behavior[Command] = {
    Behaviors.receive((ctx, msg) => {
      msg match {
        case GetAnswer(replyTo) =>
          replyTo ! GetAnswerResponse(answer)
          Behaviors.same
        case UpdateAnswer() =>
          Behaviors.same
      }
    })
  }
}
