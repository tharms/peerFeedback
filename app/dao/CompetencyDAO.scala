package dao

import controllers.Rating
import dao.exceptions.ServiceException
import dao.models.Competency
import play.api.libs.json.{JsResult, Json}

import scala.concurrent.Future

/**
 * Created by tobias on 29.06.15.
 */
object CompetencyDAO extends DocumentDAO[Competency] {
  import dao.models._
  import dao.models.JsonFormats._

  val collectionName = "Competency"

  def create(competency: Competency): Future[Either[ServiceException, Competency]] = insert(competency)

  override def ensureIndexes: Future[List[Boolean]] = Future.successful(List[Boolean]())
}
