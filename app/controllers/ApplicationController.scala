package controllers

import java.util.UUID
import java.util.concurrent.TimeoutException
import javax.inject.{Singleton, Inject}

import com.mohiva.play.silhouette.api.{ Environment, LogoutEvent, Silhouette }
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models._
import models.daos.{RegistrationDAO, OfferDAO, PartnerDAO, ClazzDAO}
import play.Play
import play.api.Logger
import play.api.cache.Cache
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current
import scala.concurrent.duration._
import play.api.libs.json._
import scala.collection.mutable.ArrayBuffer


import scala.concurrent.{Await, Future}

/**
 * The basic application controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param socialProviderRegistry The social provider registry.
 */
@Singleton
class ApplicationController @Inject()(
                                       val messagesApi: MessagesApi,
                                       val env: Environment[Partner, JWTAuthenticator],
                                       socialProviderRegistry: SocialProviderRegistry,
                                       clazzDAO: ClazzDAO,
                                       partnerDAO: PartnerDAO,
                                       offerDAO: OfferDAO)
  extends Silhouette[Partner, JWTAuthenticator] {




  def enums = UserAwareAction.async { implicit request =>
    lazy val cacheExpire = Play.application().configuration().getString("cache.expire.get.enums").toInt
    val enums:JsValue = Cache.getAs[JsValue]("enums").getOrElse{
      var recurrences = ArrayBuffer[JsValue]()
      for (d <- Recurrence.values) {
        recurrences += Json.obj(
          "id" -> d,
          "name" -> Messages("enum.clazz.recurrence."+d)
        )
      }
      var types = ArrayBuffer[JsValue]()
      for (d <- Type.values) {
        types += Json.obj(
          "id" -> d,
          "name" -> Messages("enum.clazz.type."+d)
        )
      }
      val e = Json.obj("enums" -> Json.obj(
        "clazz" -> Json.obj(
          "recurrences" -> Json.toJson(recurrences),
          "types" -> Json.toJson(types)
        )
      ))
      Cache.set("enums", e, cacheExpire.seconds)
      e
    }
    Future.successful(Ok(enums))
  }


  def offers = UserAwareAction.async { implicit request =>
    lazy val cacheExpire = Play.application().configuration().getString("cache.expire.get.offers").toInt
    val offers:List[Offer] = Cache.getAs[List[Offer]]("offers").getOrElse{
      val offers:List[Offer] = Await.result(offerDAO.list(), 5.seconds)
      Cache.set("offers", offers, cacheExpire.seconds)
      offers
    }
    Future.successful(Ok(Json.toJson(offers)))
  }
  /**
   * Returns the partner.
   *
   * @return The result to display.
   */
  def partner = SecuredAction.async { implicit request =>
    Future.successful(Ok(Json.toJson(request.identity)))
  }

  /**
   * Manages the sign out action.
   */
  def signOut = SecuredAction.async { implicit request =>
    env.eventBus.publish(LogoutEvent(request.identity, request, request2Messages))
    env.authenticatorService.discard(request.authenticator, Ok)
  }

  /**
   * Provides the desired template.
   *
   * @param template The template to provide.
   * @return The template.
   */

  def viewRestricted(template: String) = SecuredAction.async { implicit request =>
    template match {
      case "clazzes" => Future.successful(Ok(views.html.me.clazzes()))
      case "clazzesEdit" => Future.successful(Ok(views.html.me.clazzesEdit()))
      case "dashboard" => Future.successful(Ok(views.html.me.dashboard()))
      case "header" => Future.successful(Ok(views.html.me.header()))
      case "sidebar" => Future.successful(Ok(views.html.me.sidebar()))
      case _ => Future.successful(NotFound)
    }
  }

  def view(template: String) = UserAwareAction { implicit request =>
    template match {
      case "test" => Ok(views.html.me.clazzesEdit())
      case "signIn" => Ok(views.html.signIn(socialProviderRegistry))
      case "header" => Ok(views.html.header())
      case "footer" => Ok(views.html.footer())
      case _ => NotFound
    }
  }
}
