package dao


import javax.inject.Singleton
import dao.exceptions.{ResourceNotFoundException, ServiceException}
import dao.models.Competency
import play.api.libs.json.{JsArray, JsResult, Json}
import play.api.mvc.Action
import reactivemongo.api.Cursor
import play.api.Logger


import scala.concurrent.{Promise, Future}
import scala.util.{Failure, Success, Try}

/**
 * Created by tobias on 29.06.15.
 */
@Singleton
class CompetencyDAO extends DocumentDAO[Competency] {
  import dao.models._
  import dao.models.JsonFormats._

  val collectionName = "competencies"


/*
  def findByEmail(email: String): Future[Option[Competency]] =  {
    //val objectId = Json.obj("$oid" -> id.get)
    Logger.info(s"Finding user by $email")
    println(s"Finding user by $email")
    // let's do our query
    val cursor: Cursor[Competency] =
      collection.
        // find all
        find(Json.obj("employeeMail" -> email)).
        // sort them by creation date
        sort(Json.obj("created" -> -1)).
        // perform the query and get a cursor of JsObject
        cursor[Competency]

    val p = Promise[Option[Competency]]
    val f = p.future

    cursor.headOption onComplete {
      case Success(c) => p success c
      case Failure(t) => p success None
    }

    f
  }

  def find(email: String): Future[Option[Competency]] = findOne(Json.obj("employeeMail" -> email))
*/
  override def ensureIndexes: Future[List[Boolean]] = Future.successful(List[Boolean]())
}
