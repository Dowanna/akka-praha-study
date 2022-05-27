import adaptor.actor.QuestionActor
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import domain.{Answer, Tag}

import scala.concurrent.duration.FiniteDuration
import org.scalatest.wordspec.AnyWordSpecLike

class QuestionActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  import adaptor.actor.QuestionActor._

  "Question Actor" must {
    "アンサーの追加と取得ができる" in {
      val testProbe = createTestProbe[GetAllAnswersResponse]()
      val questionActor = spawn(QuestionActor())

      val answer = new Answer(
        id = "001",
        text = "ggrks",
        tags = Set(new Tag(name = "Google"), new Tag("Yahoo") )
      )
      questionActor ! AddAnswer(answer)
      questionActor ! GetAllAnswers(testProbe.ref)

      testProbe.expectMessage(GetAllAnswersResponse(Vector(answer, answer)))
    }
  }
}
