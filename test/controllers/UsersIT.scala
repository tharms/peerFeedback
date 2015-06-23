package controllers

import play.api.libs.iteratee.Iteratee

import scala.concurrent._
import duration._
import org.specs2.mutable._

import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import java.util.concurrent.TimeUnit


/**
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class UsersIT extends Specification {

  val timeout: FiniteDuration = FiniteDuration(5, TimeUnit.SECONDS)

  "Users" should {

    "insert a valid json" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(POST, "/user").withJsonBody(Json.obj(
          "firstName" -> "Jack",
          "lastName" -> "London",
          "age" -> 39,
          "active" -> true))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(CREATED)
      }
    }

    "fail inserting a non valid json" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(POST, "/user").withJsonBody(Json.obj(
          "firstName" -> 98,
          "lastName" -> "London",
          "age" -> 27))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        contentAsString(response.get) mustEqual "invalid json"
        result.header.status mustEqual BAD_REQUEST
      }
    }

    "update a valid json" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(PUT, "/user/Jack/London").withJsonBody(Json.obj(
          "firstName" -> "Jack",
          "lastName" -> "London",
          "age" -> 66,
          "active" -> true))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status must equalTo(CREATED)
      }
    }

    "update a user by id" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(PUT, "/userById/557f3a3e4468ffa85641ccf2").withJsonBody(Json.obj(
          "firstName" -> "Mark",
          "lastName" -> "Twain",
          "age" -> 33,
          "active" -> true))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)

        System.out.println("+++++++++++" + contentAsString(response.get))

        result.header.status must equalTo(CREATED)
      }
    }

    "update a user by non existent id" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(PUT, "/userById/55847c0aff189a6c06a43ddx").withJsonBody(Json.obj(
          "firstName" -> "Mark",
          "lastName" -> "Twain",
          "age" -> 33,
          "active" -> true))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)

        System.out.println("+++++++++++" + contentAsString(response.get))

        result.header.status must equalTo(NOT_FOUND)
      }
    }

    "fail updating a non valid json" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(PUT, "/user/Jack/London").withJsonBody(Json.obj(
          "firstName" -> "Jack",
          "lastName" -> "London",
          "age" -> 27))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        contentAsString(response.get) mustEqual "invalid json"
        result.header.status mustEqual BAD_REQUEST
      }
    }

    "find users" in {
      running(FakeApplication()) {
        val request = FakeRequest(GET, "/users")

        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        result.header.status mustEqual OK
      }
    }

    "find specific user" in {
      running(FakeApplication()) {
        val request = FakeRequest(GET, "/userById/55847c0aff189a6c06a43dd7")

        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)

        System.out.println("+++++++++++" + contentAsString(response.get))
        result.header.status mustEqual OK
      }
    }

  }
}