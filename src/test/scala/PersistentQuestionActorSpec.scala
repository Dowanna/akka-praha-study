import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.FreeSpecLike
import adaptor.actor.PersistentQuestionActor

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

    }
  }
}
