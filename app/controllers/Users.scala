package controllers

import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.{BSONObjectID, BSONDocument}
import reactivemongo.core.commands.GetLastError
import scala.concurrent.Future
import reactivemongo.api.Cursor
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import org.slf4j.{LoggerFactory, Logger}
import javax.inject.Singleton
import play.api.mvc._
import play.api.libs.json._

/**
 * The Users controllers encapsulates the Rest endpoints and the interaction with the MongoDB, via ReactiveMongo
 * play plugin. This provides a non-blocking driver for mongoDB as well as some useful additions for handling JSon.
 * @see https://github.com/ReactiveMongo/Play-ReactiveMongo
 */
@Singleton
class Users extends Controller with MongoController {

  private final val logger: Logger = LoggerFactory.getLogger(classOf[Users])

  /*
   * Get a JSONCollection (a Collection implementation that is designed to work
   * with JsObject, Reads and Writes.)
   * Note that the `collection` is not a `val`, but a `def`. We do _not_ store
   * the collection reference to avoid potential problems in development with
   * Play hot-reloading.
   */
  def collection: JSONCollection = db.collection[JSONCollection]("users")

  // ------------------------------------------ //
  // Using case classes + Json Writes and Reads //
  // ------------------------------------------ //

  import models._
  import models.JsonFormats._

  def createUser = Action.async(parse.json) {
    request =>
      /*
       * request.body is a JsValue.
       * There is an implicit Writes that turns this JsValue as a JsObject,
       * so you can call insert() with this JsValue.
       * (insert() takes a JsObject as parameter, or anything that can be
       * turned into a JsObject using a Writes.)
       */
      request.body.validate[User].map {
        user =>
          // `user` is an instance of the case class `models.User`
          collection.insert(user).map {
            lastError =>
              logger.debug(s"Successfully inserted with LastError: $lastError")
              Created(s"User Created")
          }
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  def updateUser(firstName: String, lastName: String) = Action.async(parse.json) {
    println(">>>>>>>>>>>>> updateUser " + firstName + " " + lastName)
    request =>
      request.body.validate[User].map {
        user =>
          // find our user by first name and last name
          val nameSelector = Json.obj("firstName" -> firstName, "lastName" -> lastName)
          collection.update(nameSelector, user).map {
            lastError =>
              println(s"Successfully updated with LastError: $lastError")
              logger.debug(s"Successfully updated with LastError: $lastError")
              Created(s"User Updated")
          }
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  def updateUserById(id: String) = Action.async(parse.json) {
    println(">>>>>>>>>>>>> updateUserById")
    request =>
      request.body.validate[User].map {
        user =>
          // find our user by id
          val idSelector = Json.obj("_id" -> Json.obj("$oid" -> id))
          val futureResult = collection.update(idSelector, user)
          futureResult.map {
            case t => t.inError match {
              case true => InternalServerError("%s".format(t))
              case false => { t.n match {
                case 1 => Created(s"User Updated")
                case 0 => NotFound(s"No such user $id")
              } }
            }
          }
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  def findUserById(id: String) = findUsers(Some(id))

  def findUsers(id: Option[String]) = Action.async {
    val objectId = Json.obj("$oid" -> id.get)
    // let's do our query
    val cursor: Cursor[User] =
      collection.
        // find all
        find(id.foldLeft(Json.obj("active" -> true))((json, id) => json ++ Json.obj("_id" -> objectId))).
        // sort them by creation date
        sort(Json.obj("created" -> -1)).
        // perform the query and get a cursor of JsObject
        cursor[User]

    // gather all the JsObjects in a list
    val futureUsersList: Future[List[User]] = cursor.collect[List]()

    // transform the list into a JsArray
    val futurePersonsJsonArray: Future[JsArray] = futureUsersList.map { users =>
      Json.arr(users)
    }
    // everything's ok! Let's reply with the array
    futurePersonsJsonArray.map {
      users =>
        Ok(users(0))
    }
  }

}
