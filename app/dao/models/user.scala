package dao.models

import org.joda.time.DateTime
import play.api.data.validation.ValidationError
import play.api.libs.json._

import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._


case class User(        override var _id: Option[BSONObjectID] = None,
                        override var created: Option[DateTime] = None,
                        override var updated: Option[DateTime] = None,
                        age: Int,
                        firstName: String,
                        lastName: String,
                        email: String,
                        active: Boolean) extends TemporalModel

case class Competency(  override var _id: Option[BSONObjectID] = None,
                        override var created: Option[DateTime] = None,
                        override var updated: Option[DateTime] = None,
                        employeeMail: String,
                        name: String,
                        description: String,
                        assessments: Seq[BSONObjectID]) extends TemporalModel

case class Assessment ( override var _id: Option[BSONObjectID] = None,
                        override var created: Option[DateTime] = None,
                        override var updated: Option[DateTime] = None,
                        assessedBy: String,
                        rating: Int) extends TemporalModel


object JsonFormats {
  import play.api.libs.json.Json

  // Generates Writes and Reads for Feed and User thanks to Json Macros
  implicit val userFormat = Json.format[User]
  implicit val assessmentFormat = Json.format[Assessment]
  implicit val competencyFormat = Json.format[Competency]


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