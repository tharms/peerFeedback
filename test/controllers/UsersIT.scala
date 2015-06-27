package controllers

import play.api.libs.iteratee.Iteratee

import scala.concurrent._
import duration._
import org.specs2.mutable._

import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import java.util.concurrent.TimeUnit
import org.specs2.execute.AsResult

/**
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class UsersIT extends Specification {

  val timeout: FiniteDuration = FiniteDuration(5, TimeUnit.SECONDS)

  def resetTheSystem() = {
      val enc = java.net.URLEncoder.encode("jack@gmail.com", "UTF-8")
      val request = FakeRequest(DELETE, s"/userById/$enc")
      val response = route(request)
      val result = Await.result(response.get, timeout)
      println("+++++++++++" + contentAsString(response.get))
      response.isDefined mustEqual true
   }

  "Users" should {

    "insert a valid json" in {
      running(FakeApplication()) {
        resetTheSystem()

        val request = FakeRequest.apply(POST, "/user").withJsonBody(Json.obj(
          "firstName" -> "Jack",
          "lastName" -> "London",
          "email" -> "jack@gmail.com",
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
          "email" -> "jack@gmail.com",
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
        val enc = java.net.URLEncoder.encode("jack@gmail.com", "UTF-8")
        val request = FakeRequest.apply(PUT, s"/userById/$enc").withJsonBody(Json.obj(
          "firstName" -> "Mark",
          "lastName" -> "Twain",
          "email" -> "jack@gmail.com",
          "age" -> 33,
          "active" -> true))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)

        println("+++++++++++" + contentAsString(response.get))

        result.header.status must equalTo(CREATED)
      }
    }

    "update a user by non existent id" in {
      running(FakeApplication()) {
        val request = FakeRequest.apply(PUT, "/userById/blabla").withJsonBody(Json.obj(
          "firstName" -> "Mark",
          "lastName" -> "Twain",
          "age" -> 33,
          "active" -> true))
        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)
        println("+++++++++++" + contentAsString(response.get))
        result.header.status must equalTo(BAD_REQUEST)
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
/*
    "delete a user by id" in {
      running(FakeApplication()) {
        val enc = java.net.URLEncoder.encode("jack@gmail.com", "UTF-8")
        val request = FakeRequest(DELETE, s"/userById/$enc")
        val response = route(request)
        val result = Await.result(response.get, timeout)
        println("+++++++++++" + contentAsString(response.get))
        response.isDefined mustEqual true
      }
    }
 */
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
        val enc = java.net.URLEncoder.encode("jack@gmail.com", "UTF-8")
        val request = FakeRequest(GET, s"/userById/$enc")

        val response = route(request)
        response.isDefined mustEqual true
        val result = Await.result(response.get, timeout)

        System.out.println("+++++++++++" + contentAsString(response.get))
        result.header.status mustEqual OK
      }
    }

  }
}