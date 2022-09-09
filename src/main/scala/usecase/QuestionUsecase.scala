package usecase

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior
import route.QuestionRoutes._
import domain.{Question, Tag}
import akka.actor.typed.ActorRef
import adaptor.actor.QuestionActor
import adaptor.actor.QuestionAggregates
import adaptor.actor.PersistentQuestionActor
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.Scheduler
import akka.util.Timeout
import java.time.Duration
import akka.remote.WireFormats.FiniteDuration
import scala.concurrent.duration

object QuestionUsecase {
  sealed trait Command
  final case class Create(questionRequest: QuestionRequest, replyTo: ActorRef[Response]) extends Command

  final case class Get(id: String, replyTo: ActorRef[Response]) extends Command

  sealed trait Response
  final case class SuccessResponse(questionResponse: QuestionResponse) extends Response
  final case class FailedResponse() extends Response

  implicit val timeout = Timeout.create(Duration.ofSeconds(3));

  def apply(questionAggregatesRef: ActorRef[PersistentQuestionActor.Command]): Behavior[Command] = {
    registry(questionAggregatesRef)
  }

  private def registry(questionAggregatesRef: ActorRef[PersistentQuestionActor.Command]): Behavior[Command] = {
    Behaviors.receive { (ctx, msg) =>
      msg match {
        case Create(questionRequest, replyTo) => {
          Question(
            id = questionRequest.id,
            title = questionRequest.title,
            body = questionRequest.body,
            Set.empty,
            tags = questionRequest.tags.fold(Set.empty[Tag])(tagRequestSet => tagRequestSet.map(tagRequest => Tag(tagRequest.name)))
          ) match {
            case Left(_)         => replyTo ! FailedResponse()
            case Right(question) =>
              // 永続化アクターにQuestionを渡す
              // Mainでspawnしているのでそっちを参照したい　 -> usecaseに引数でわたす。
              // val persistentQuestionActor = spawn(QuestionAggregates.behavior(QuestionActor.name)(PersistentQuestionActor.behavior))

              ctx.scheduleOnce(duration.FiniteDuration(3, "hoge"), questionAggregatesRef, PersistentQuestionActor.Command)

              // questionAggregates.persistentQuestionActor ! PersistentQuestionActor.CreateQuestion(question)
              replyTo ! SuccessResponse(
                QuestionResponse(
                  id = question.id,
                  title = question.title,
                  body = question.body,
                  answers = question.answers.map(answer =>
                    AnswerResponse(id = answer.id, text = answer.text, tags = answer.tags.map(tag => TagResponse(tag.name)))
                  ),
                  tags = question.tags.map(tag => TagResponse(tag.name))
                )
              )
          }
          Behaviors.same
        }
      }
    }
  }
}
