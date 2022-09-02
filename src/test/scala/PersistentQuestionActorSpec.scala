import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.FreeSpecLike
import adaptor.actor.PersistentQuestionActor
import domain.Answer
import adaptor.actor.QuestionActor

class PersistentQuestionActorSpec extends ScalaTestWithActorTestKit with FreeSpecLike {

  override protected def beforeAll(): Unit = {
    super.beforeAll()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
  }

  "PersistentQuestion" - {
    "保存できる" - {

      val questionRef = spawn(PersistentQuestionActor.behavior("question_1"))

      val testProbe = createTestProbe[QuestionActor.CommandResponse]()

      questionRef ! PersistentQuestionActor.AddAnswer("123", Answer("id", "text", Set.empty))
      questionRef ! PersistentQuestionActor.Get("123", testProbe.ref)

      testProbe.expectMessageType[QuestionActor.GetQuestion]

    }
  }
}
