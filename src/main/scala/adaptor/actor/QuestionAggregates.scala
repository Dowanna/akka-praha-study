package adaptor.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import domain.Question

object QuestionAggregates {

  val name = "wallets"

  def behavior(
      name: String => String,
  )(behaviorF: (String) => Behavior[PersistentQuestionActor.Command]): Behavior[PersistentQuestionActor.Command] = {
    Behaviors.setup { ctx =>
      def createAndSend(questionId: String): ActorRef[PersistentQuestionActor.Command] = {
        ctx.child(name(questionId)) match {
          case None =>
            // 子アクター作成
            ctx.spawn(behaviorF(questionId), name = name(questionId))
          case Some(ref) =>
            // 子アクターの参照取得
            ref.asInstanceOf[ActorRef[PersistentQuestionActor.Command]]
        }
      }

      Behaviors.receiveMessage[PersistentQuestionActor.Command] { msg =>
        createAndSend(msg.questionId) ! msg
        Behaviors.same
      }
    }
  }

}
