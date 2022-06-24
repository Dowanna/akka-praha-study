package route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import usecase.QuestionUsecase
import domain.Question

class QuestionRoutes(uscease: QuestionUsecase) {
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import spray.json.DefaultJsonProtocol._

  case class AnswerRequest(val id: String, val text: String, val tags: Set[TagRequest])
  case class QuestionRequest(val id: String, val title: String, val body: String, val answers: Set[AnswerRequest], val tags: Set[TagRequest])
  case class TagRequest(val name: String) // tagとanswerが循環参照しているような時は、どうやってjsonFormat作るんだろう？（片方を先に生成しないともう片方が生成できないデッドロック気味な状態）
  implicit val tagJsonFormat = jsonFormat1(TagRequest)
  implicit val answerJsonFormat = jsonFormat3(AnswerRequest)
  implicit val questionJsonFormat = jsonFormat5(QuestionRequest)

  def getUsers(): String = {
    "hoge"
  }
  val questionRoutes: Route =
    pathPrefix("questions") {
      concat(
        get {
          complete(getUsers())
        },
        post {
          entity(as[QuestionRequest]) { questionRequest =>
            onSuccess(createUser(user)) { performed =>
              complete((StatusCodes.Created, performed))
            }
          }
        }
      )
      // concat(
      //   //#users-get-delete
      //   pathEnd {
      //     concat(
      //       get {
      //         complete(getUsers())
      //       },
      //       post {
      //         entity(as[User]) { user =>
      //           onSuccess(createUser(user)) { performed =>
      //             complete((StatusCodes.Created, performed))
      //           }
      //         }
      //       })
      //   },
      //   //#users-get-delete
      //   //#users-get-post
      //   path(Segment) { name =>
      //     concat(
      //       get {
      //         //#retrieve-user-info
      //         rejectEmptyResponse {
      //           onSuccess(getUser(name)) { response =>
      //             complete(response.maybeUser)
      //           }
      //         }
      //         //#retrieve-user-info
      //       },
      //       delete {
      //         //#users-delete-logic
      //         onSuccess(deleteUser(name)) { performed =>
      //           complete((StatusCodes.OK, performed))
      //         }
      //         //#users-delete-logic
      //       })
      //   })
      // #users-get-delete
    }
  // #all-routes
}
