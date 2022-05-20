package adaptor.actor
import akka.actor.typed.{Behavior}
import akka.actor.typed.scaladsl.Behaviors
import domain.Answer

object AnswerActor {
  sealed trait Command

  def apply(answer: Answer): Behavior[Command] = {
    hoge(answer)
  }

  private def hoge(answer: Answer) {
    Behaviors.receive((ctx, msg) => {
      msg match {
        case GetAnswer =>
          answer
          Behaviors.same
        case UpdateAnswer =>
          hoge(answer.updateAnswer())
      }
    })
  }
}
