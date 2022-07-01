package route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import usecase.QuestionUsecase
import domain.Question
import usecase.QuestionUsecase.Hoge

import scala.concurrent.Future
import akka.actor.typed.ActorRef
import domain.Tag

class QuestionRoutes(usecase: ActorRef[QuestionUsecase.Command]) {
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import spray.json.DefaultJsonProtocol._
  import scala.concurrent.ExecutionContext.Implicits.global

  case class TagRequest(val name: String) // tagとanswerが循環参照しているような時は、どうやってjsonFormat作るんだろう？（片方を先に生成しないともう片方が生成できないデッドロック気味な状態）
  case class AnswerRequest(val id: String, val text: String, val tags: Set[TagRequest])
  case class QuestionRequest(val id: String, val title: String, val body: String, val tags: Set[TagRequest] = Set.empty[TagRequest])
  implicit val tagJsonFormat = jsonFormat1(TagRequest)
  implicit val answerJsonFormat = jsonFormat3(AnswerRequest)
  implicit val questionJsonFormat = jsonFormat4(QuestionRequest)

  case class TagResponse(val name: String) // tagとanswerが循環参照しているような時は、どうやってjsonFormat作るんだろう？（片方を先に生成しないともう片方が生成できないデッドロック気味な状態）
  case class AnswerResponse(val id: String, val text: String, val tags: Set[TagResponse])
  case class QuestionResponse(val id: String, val title: String, val body: String, val answers: Set[AnswerResponse], val tags: Set[TagResponse])
  implicit val tagResponseJsomFormat = jsonFormat1(TagResponse)
  implicit val answerResponseJsomFormat = jsonFormat3(AnswerResponse)
  implicit val questionResponseJsomFormat = jsonFormat5(QuestionResponse)

  def getUsers(): String = {
    "hoge"
  }

  def createQuestion(questionRequest: QuestionRequest): Future[Option[QuestionResponse]] = {
    Future {
      usecase ! Hoge()

      // 一旦仮のデータを返す
      Question("id", "title", "body", Set.empty, Set(Tag("test"))) match {
        case Left(_) => None
        case Right(question) => Some(
          QuestionResponse(
            id = question.id,
            title = question.title,
            body = question.body,
            answers = question.answers.map(answer => AnswerResponse(id = answer.id, text = answer.text, tags = answer.tags.map(tag => TagResponse(tag.name)))),
            tags = question.tags.map(tag => TagResponse(tag.name))))
      }
    }
  }

  val questionRoutes: Route =
    pathPrefix("questions") {
      concat(
        get {
          complete(getUsers())
        },
        post {
          entity(as[QuestionRequest]) { questionRequest =>
            onSuccess(createQuestion(questionRequest)) {
              case Some(question) => complete(question)
              case None => complete(StatusCodes.BadRequest)
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
