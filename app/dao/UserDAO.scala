package dao

import dao.models.User
import reactivemongo.api.indexes.IndexType.Ascending

import scala.concurrent.Future
import play.api.libs.json._

/**
 * Created by tobias on 29.06.15.
 */
object UserDAO extends DocumentDAO[User] {

  import dao.models._
  import dao.models.JsonFormats._

  val collectionName = "users"

  def findByEmail(email: String): Future[Option[User]] = findOne(Json.obj("email" -> email))

  override def ensureIndexes = {
    Future.sequence(
      List(
        ensureIndex(List("email" -> Ascending), unique = true)
      )
    )
  }
}
