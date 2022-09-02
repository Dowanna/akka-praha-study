package adaptor.actor

import adaptor.actor.QuestionActor.GetQuestion
import akka.actor.typed.ActorRef
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import domain.Answer
import domain.Question

object PersistentQuestionActor {
  sealed trait Command {
    val questionId: String
  }
  final case class AddAnswer(questionId: String, answer: Answer) extends Command
  final case class Get(questionId: String, replyTo: ActorRef[QuestionActor.CommandResponse]) extends Command

  final case class CreateQuestion(question: Question) extends Command

  sealed trait Event
  final case class AddedAnswerToQuestion(question: Question) extends Event

  final case class QuestionCreated(question: Question) extends Event

  case object AddAnswerFailed extends Event

  sealed trait State
  case object EmptyState extends State
  case class DefinedState(question: Question) extends State

  def behavior(id: String) = {
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId("question", id), // 他のpersistent-actorと被ったらどうなるんだろう?
      emptyState = EmptyState,
      commandHandler = commandHandler,
      eventHandler = eventHandler
      // commandHandler = (state, command) => throw new NotImplementedError("TODO: process the command & return an Effect"),
      // eventHandler = (state, event) => throw new NotImplementedError("TODO: process the event return the next state")
    )
  }

  val commandHandler: (State, Command) => Effect[Event, State] = (state, command) =>
    (state, command) match {
      case (EmptyState, CreateQuestion(question)) => {
        Effect.persist(QuestionCreated(question))
      }
      case (EmptyState, AddAnswer(questionId, answer)) => {
        Question(
          id = "id",
          title = "title",
          body = "body",
          answers = Set(answer),
          tags = Set.empty
        ) match {
          case Left(value) => Effect.persist(AddAnswerFailed)
          case Right(value) =>
            Effect.persist(AddedAnswerToQuestion(value))
        }
      }
      case (DefinedState(question), AddAnswer(questionId, answer)) => {
        Question(
          id = questionId,
          title = question.title,
          body = question.body,
          answers = question.answers + answer,
          tags = question.tags
        ) match {
          case Left(value) => Effect.persist(AddAnswerFailed)
          case Right(value) =>
            Effect.persist(AddedAnswerToQuestion(value))
        }
      }
      case (DefinedState(question), Get(_, replyTo)) => {
        replyTo ! GetQuestion(question)
        Effect.none
      }
      case _ => {
        Effect.none
      }
    }

  val eventHandler: (State, Event) => State = (state, event) =>
    event match {
      case AddedAnswerToQuestion(question) => DefinedState(question)
      case AddAnswerFailed                 => state
      case QuestionCreated(question)       => DefinedState(question)
    }
  // fixme: sbt runするとこのエラーが出る
//     12:55:49.025 [PersistentQuestionActorSpec-akka.actor.default-dispatcher-3] DEBUG akka.persistence.typed.internal.EventSourcedBehaviorImpl - Recovery for persistenceId [PersistenceId(question|question_1)] took 254.1 ms
// 12:55:49.038 [PersistentQuestionActorSpec-akka.actor.default-dispatcher-3] ERROR akka.actor.SupervisorStrategy - TODO: process the command & return an Effect
// scala.NotImplementedError: TODO: process the command & return an Effect

}
