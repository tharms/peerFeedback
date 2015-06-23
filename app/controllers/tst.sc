import controllers.Users
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import play.mvc.Controller
import reactivemongo.bson.BSONObjectID

val c =  BSONObjectID("507f1f77bcf86cd799439011")
c.toString()
val id:Option[BSONObjectID] = Some(c)
val o = Json.obj("active" -> true)
