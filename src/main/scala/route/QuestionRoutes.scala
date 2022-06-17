package route

import akka.http.scaladsl.server.Directives.{onSuccess, pathPrefix}
import akka.http.scaladsl.server.Route

class QuestionRoutes {
  //#all-routes
  //#users-get-post
  //#users-get-delete
  val userRoutes: Route =
  pathPrefix("questions") {
    concat(
      //#users-get-delete
      pathEnd {
        concat(
          get {
            complete(getUsers())
          },
          post {
            entity(as[User]) { user =>
              onSuccess(createUser(user)) { performed =>
                complete((StatusCodes.Created, performed))
              }
            }
          })
      },
      //#users-get-delete
      //#users-get-post
      path(Segment) { name =>
        concat(
          get {
            //#retrieve-user-info
            rejectEmptyResponse {
              onSuccess(getUser(name)) { response =>
                complete(response.maybeUser)
              }
            }
            //#retrieve-user-info
          },
          delete {
            //#users-delete-logic
            onSuccess(deleteUser(name)) { performed =>
              complete((StatusCodes.OK, performed))
            }
            //#users-delete-logic
          })
      })
    //#users-get-delete
  }
  //#all-routes
}
