package models
import play.api.libs.json._

import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._

import scala.util.Try

case class User( _id: Option[BSONObjectID],
                 age: Int,
                 firstName: String,
                 lastName: String,
                 active: Boolean)
case class Review( from: BSONObjectID,
                   to: BSONObjectID,
                   skill1: String,
                   level1: Int)


object JsonFormats {
  import play.api.libs.json.Json

  // Generates Writes and Reads for Feed and User thanks to Json Macros
  implicit val userFormat = Json.format[User]
  implicit val reviewFormat = Json.format[Review]
/*
  implicit object BSONObjectIDFormat extends Format[BSONObjectID] {
    def writes(objectId: BSONObjectID): JsValue = {
      println(s"========================Successfully converted BSONObjectID to JsValue: $objectId")
      Json.obj("$oid" -> JsString(objectId.stringify))
    }

    def reads(json: JsValue): JsResult[BSONObjectID] = json match {
      case JsString(x) => {
        val maybeOID: Try[BSONObjectID] = BSONObjectID.parse(x)
        if (maybeOID.isSuccess) {
          val success = maybeOID.get
          println(s"=======================Successfully converted JsValue to BSONObjectID: $success")
          JsSuccess(success)
        }
        else {
          JsError("Expected BSONObjectID as JsString")
        }
      }

      case JsObject(Seq((_, oid))) =>
        reads(oid)

      case _ => JsError("Expected BSONObjectID as JsString")
    }
  }
  */
}