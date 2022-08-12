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

  sealed trait Event
  final case class AddedAnswerToQuestion(question: Question) extends Event
  case object AddAnswerFailed extends Event

  final case class State(question: Question)

  def behavior(id: String, question: Question) = {
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId("question", id), // 他のpersistent-actorと被ったらどうなるんだろう?
      emptyState = State(question),
      commandHandler = (state, command) => throw new NotImplementedError("TODO: process the command & return an Effect"),
      eventHandler = (state, event) => throw new NotImplementedError("TODO: process the event return the next state")
    )
  }

  val commandHandler: (State, Command) => Effect[Event, State] = (state, command) =>
    command match {
      case AddAnswer(answer) => {
        Question(id = state.question.id, title = state.question.title, body = state.question.body, answers = state.question.answers + answer, tags = state.question.tags) match {
          case Left(value) => Effect.persist(AddAnswerFailed)
          case Right(value) =>
            Effect.persist(AddedAnswerToQuestion(value))
        }
      }
      case Get(replyTo) =>
        replyTo ! GetQuestion(state.question)
        Effect.none
    }
  val eventHandler: (State, Event) => State = (state, event) =>
    event match {
      case AddedAnswerToQuestion(question) => State(question)
      case AddAnswerFailed => state
    }
}
