package route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import usecase.QuestionUsecase
import domain.Question

class QuestionRoutes(uscease: QuestionUsecase) { // QuestionUsecaseを引数に定義するためにはobject QuestionUsecaseだけではNG。class QuestionUsecaseを定義する必要があるが、理由はイマイチわかってない
  // import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
  import JsonFormats._

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
          entity(as[Question]) { user =>
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
