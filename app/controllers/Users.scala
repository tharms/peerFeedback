package controllers

import dao.UserDAO
import dao.models.User
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONObjectID, BSONDocument}
import reactivemongo.core.commands.{LastError, GetLastError}
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
  def collection: JSONCollection = {
    val coll = db.collection[JSONCollection]("users")
    coll.indexesManager.ensure(Index(List("email" -> IndexType.Ascending), unique = true))
    coll
  }


  // ------------------------------------------ //




  // Using case classes + Json Writes and Reads //
  // ------------------------------------------ //

  import dao.models._
  import dao.models.JsonFormats._

  def isValidEmail(email: String): Boolean =
    if("""(?=[^\s]+)(?=(\w+)@([\w\.]+))""".r.findFirstIn(email) == None)false else true

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
          val futureResult = {
            if (isValidEmail(user.email)) {
              // find our user by id
              collection.insert(user)
            } else {
              Future(LastError(false, None, Some(99), Some(s"Invalid email: $user.email"), None, 0, false))
            }
          }
          futureResult.map {
            case t => t.inError match {
              case true => {
                t.code match {
                  case Some(99) => BadRequest("%s".format(t))
                  case _ => InternalServerError("%s".format(t))
                }
              }
              case false => Created("User inserted")
            }
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


  def deleteUserById(id: String) = Action.async {
    val futureResult = collection.remove(Json.obj("email" -> id), firstMatchOnly = true)
    futureResult.map {
      case t => t.inError match {
        case true => InternalServerError("%s".format(t))
        case false => {
          t.n match {
            case 1 => Ok(s"$id deleted")
            case 0 => NotFound(s"No such user $id")
          }
        }
      }
    }
  }

  def updateUserById(email: String) = Action.async(parse.json) {
    println(">>>>>>>>>>>>> updateUserById")
    request =>
      request.body.validate[User].map {
        user =>
          val futureResult = {
           if (isValidEmail(email) && isValidEmail(user.email)) {
              // find our user by id
              val idSelector = Json.obj("email" -> email)
              collection.update(idSelector, user)
            } else {
              Future(LastError(false, None, Some(99), Some(s"Invalid email: $email"), None, 0, false))
            }
          }

          futureResult.map {
            case t => t.inError match {
              case true => {
                t.code match {
                  case Some(99) => BadRequest("%s".format(t))
                  case _ => InternalServerError("%s".format(t))
                }
              }
              case false => {
                t.n match {
                  case 1 => Created(s"User Updated")
                  case 0 => NotFound(s"No such user $email")
                }
              }
            }
          }
      }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  def findUserByEmail(email: String) = findUsers(Some(email))



  def findUsers(id: Option[String]) = Action.async {
    //val objectId = Json.obj("$oid" -> id.get)
    logger.info(s"Finding user by $id")
    println(s"Finding user by $id")
    // let's do our query
    val cursor: Cursor[User] =
      collection.
        // find all
        find(id.foldLeft(Json.obj("active" -> true))((json, _id) => json ++ Json.obj("email" -> id))).
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
