package controllers

import java.util.concurrent.TimeUnit

import org.specs2.mutable.{BeforeAfter, Specification}
import play.api.libs.json.Json
import play.api.test.FakeApplication
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration

import org.specs2.execute.AsResult


/**
 * Created by tobias on 28.06.15.
 */
class RatingIT extends Specification {
  val timeout: FiniteDuration = FiniteDuration(5, TimeUnit.SECONDS)

   def resetTheSystem() = {
    val enc = java.net.URLEncoder.encode("mark@gmail.com", "UTF-8")
    val delRequest = FakeRequest(DELETE, s"/userById/$enc")
    val delResponse = route(delRequest)
    val delResult = Await.result(delResponse.get, timeout)
    println("+++++++++++" + contentAsString(delResponse.get))
    delResponse.isDefined mustEqual true

    val request = FakeRequest.apply(POST, "/user").withJsonBody(Json.obj(
      "firstName" -> "Mark",
      "lastName" -> "Twain",
      "email" -> "mark@gmail.com",
      "age" -> 56,
      "active" -> true))
    val response = route(request)
    response.isDefined mustEqual true
    val result = Await.result(response.get, timeout)
     println("+++++++++++" + contentAsString(response.get))
    result.header.status must equalTo(CREATED)
  }

  "Users" should  {

    "Create a new Competency" in {
      running(FakeApplication()) {
        resetTheSystem()

        val request = FakeRequest(POST, "/competency").withJsonBody(Json.obj(
          "employeeMail" -> "mark@gmail.com",
          "name" -> "Scala skills Updated",
          "description" -> "Scala language and tools updated",
          "assessments" -> Json.arr()
        ))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        println("+++++++++++" + contentAsString(response.get))
        result.header.status must equalTo(CREATED)
      }
    }

    "Find a competency" in {
      running(FakeApplication()) {
        val enc = java.net.URLEncoder.encode("mark@gmail.com", "UTF-8")
        val request = FakeRequest(GET, s"/competency/$enc")
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        println("+++++++++++" + contentAsString(response.get))
        result.header.status must equalTo(OK)
      }
    }


    "Update a competency" in {
      running(FakeApplication()) {
        val enc = java.net.URLEncoder.encode("mark@gmail.com", "UTF-8")
        val request = FakeRequest(PUT, s"/competency/$enc").withJsonBody(Json.obj(
          "employeeMail" -> "mark@gmail.com",
          "name" -> "Scala skills",
          "description" -> "Scala language and tools",
          "assessments" -> Json.arr()
        ))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        println("+++++++++++" + contentAsString(response.get))
        result.header.status must equalTo(OK)
      }
    }

    "Delete a competency" in {
      running(FakeApplication()) {
        val enc = java.net.URLEncoder.encode("mark@gmail.com", "UTF-8")
        val request = FakeRequest(DELETE, s"/competency/$enc")
        val response = route(request)
        val result = Await.result(response.get, timeout)
        println("+++++++++++" + contentAsString(response.get))
        result.header.status must equalTo(OK)
      }
    }

  }
}
