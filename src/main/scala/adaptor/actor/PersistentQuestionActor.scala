package adaptor.actor

import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import domain.Answer

object PersistentQuestionActor {
  sealed trait Command
  final case class AddAnswer(answer: Answer) extends Command

  sealed trait Event

  final case class State()

  def Behavior = {
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId("question"),
      emptyState = State(),
      commandHandler = (state, command) => throw new NotImplementedError("TODO: process the command & return an Effect"),
      eventHandler = (state, event) => throw new NotImplementedError("TODO: process the event return the next state")
    )
  }

  val commandHandler: (State, Command) => Effect[Event, State] = (state, command) =>
    command match {

    }
}
