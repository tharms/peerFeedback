package controllers

import javax.inject.{Inject, Singleton}

import dao.exceptions.{ResourceNotFoundException, ServiceException}
import dao.models.Competency
import dao.{DocumentDAO, CompetencyDAO}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.Cursor
import services.UUIDGenerator

import scala.concurrent.Future

/**
 * The Reviews controllers encapsulates the Rest endpoints and the interaction with the MongoDB, via ReactiveMongo
 * play plugin. This provides a non-blocking driver for mongoDB as well as some useful additions for handling JSon.
 * @see https://github.com/ReactiveMongo/Play-ReactiveMongo
 */
@Singleton
class Rating @Inject() (competencyDAO: CompetencyDAO) extends Controller with MongoController {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[Rating])


  // ------------------------------------------ //
  // Using case classes + Json Writes and Reads //
  // ------------------------------------------ //

  import dao.models.JsonFormats._
  import dao.models._

  def createCompetency = Action.async(parse.json) {
    request =>
      request.body.validate[Competency].map {
        competency =>
          competencyDAO.insert(competency).map {
            case Left(ex) => InternalServerError("%s".format(ex.message))
            case Right(c) => Created(s"$c")
          }
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  def getCompetency(email: String) = Action.async {
    competencyDAO.findOne(Json.obj("employeeMail" -> email)) map {
      case Some(c) => Ok(s"$c")
      case None => NotFound(email)
    }
  }

  def updateCompetency(email: String) = Action.async(parse.json) {
    request =>
      request.body.validate[Competency].map {
        competency =>
          upsert(email, competency).map {
            case Left(ex) => InternalServerError("%s".format(ex.message))
            case Right(c) => Ok(s"$c")
          }
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  def deleteCompetency(email: String) = Action.async {
    delete(email).map {
      case Left(ex) => InternalServerError("%s".format(ex.message))
      case Right(c) => Ok(s"$c")
    }
  }

  def upsert(email: String, competency: Competency): Future[Either[ServiceException, Competency]] = {
    competencyDAO.findOne(Json.obj("employeeMail" -> email)) flatMap  {
      case Some(comp) => competencyDAO.update(comp._id.get.stringify, competency)
      case None => competencyDAO.insert(competency)
    }
  }

  def delete(email: String): Future[Either[ServiceException, Boolean]] = {
    competencyDAO.findOne(Json.obj("employeeMail" -> email)) flatMap  {
      case Some(comp) => competencyDAO.remove(comp._id.get.stringify)
      case None => Future.successful(Left(ResourceNotFoundException(email)))
    }
  }


}
