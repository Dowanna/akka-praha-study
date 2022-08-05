package adaptor.actor

package wallet.adaptor.typed

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import domain.Question

object WalletAggregates {

  val name = "wallets"

  def behavior(
      name: String => String,
  )(behaviorF: (id: String, question: Question) => Behavior[PersistentQuestionActor.Command]): Behavior[CommandRequest] = {
    Behaviors.setup { ctx =>
      def createAndSend(questionId: String): ActorRef[CommandRequest] = {
        ctx.child(QuestionActor.name(walletId)) match {
          case None =>
            // 子アクター作成
            ctx.spawn(behaviorF(walletId), name = name(questionId))
          case Some(ref) =>
            // 子アクターの参照取得
            ref.asInstanceOf[ActorRef[CommandRequest]]
        }
      }

      Behaviors.receiveMessage[CommandRequest] { msg =>
        createAndSend(msg.questionId) ! msg
        Behaviors.same
      }
    }
  }

}
