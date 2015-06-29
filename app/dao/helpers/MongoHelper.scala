package core.db

import play.api.Play.current
import play.modules.reactivemongo.ReactiveMongoPlugin

import reactivemongo.bson.{BSONObjectID, BSONValue}

import dao.helpers.ContextHelper

/**
 * Helper around `MongoDB` resources.
 * 
 * @author  Pedro De Almeida (almeidap)
 */
trait MongoHelper extends ContextHelper{

	lazy val db = ReactiveMongoPlugin.db

}

object MongoHelper extends MongoHelper {

	def identify(bson: BSONValue) = bson.asInstanceOf[BSONObjectID].stringify

}
