package util

// import com.example.UserRegistry.ActionPerformed

//#json-formats
import spray.json.DefaultJsonProtocol
import domain.Question
import domain.Answer

object JsonFormats extends DefaultJsonProtocol {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  // 本当は別のところに書くべきだけど
  final case class ActionPerformed(description: String)

  implicit val answerJsonFormat = jsonFormat3(Answer.apply)
  implicit val userJsonFormat = jsonFormat5(Question.apply)
//   implicit val usersJsonFormat = jsonFormat1(Users)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
//#json-formats
