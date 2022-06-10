import adaptor.actor.QuestionActor
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import domain.{Answer, Tag, Question, AddAnswerError}

import scala.concurrent.duration.FiniteDuration
import org.scalatest.wordspec.AnyWordSpecLike

class QuestionActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  import adaptor.actor.QuestionActor._

  "Question Actor" must {
    "アンサーの追加と取得ができる" in {
      val testProbe = createTestProbe[GetAllAnswersResponse]()
      val testProbeForError = createTestProbe[AddAnswerError]()
      val testQuestion = Question("id", "title", "body", Set.empty, Set(Tag("test")))

      testQuestion match {
        case Left(_) =>
        case Right(question) =>
          val questionActor = spawn(QuestionActor(question))

          val answer = new Answer(
            id = "001",
            text = "ggrks",
            tags = Set(new Tag(name = "Google"), new Tag("Yahoo"))
          )
          questionActor ! AddAnswer(answer, testProbeForError.ref)
          questionActor ! GetAllAnswers(testProbe.ref)

          testProbe.expectMessage(GetAllAnswersResponse(Vector(answer)))
      }
    }
    "2つ以上のアンサーを追加しようとするとエラーになる" in {
      val testProbeForError = createTestProbe[AddAnswerError]()
      val testQuestion = Question("id", "title", "body", Set.empty, Set(Tag("test")))

      testQuestion match {
        case Left(_) =>
        case Right(question) =>
          val questionActor = spawn(QuestionActor(question))

          val answer = new Answer(
            id = "001",
            text = "ggrks",
            tags = Set(new Tag(name = "Google"), new Tag("Yahoo"))
          )
          val answer2 = new Answer(
            id = "002",
            text = "yahooで検索しろks",
            tags = Set(new Tag(name = "Yahoo"))
          )
          questionActor ! AddAnswer(answer, testProbeForError.ref)
          questionActor ! AddAnswer(answer2, testProbeForError.ref)

          testProbeForError.expectMessage(AddAnswerError(tooManyAnswers = true))
      }
    }
  }
}
