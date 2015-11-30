package controllers

import java.util.UUID
import java.util.concurrent.TimeoutException
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.{Environment, LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models._
import models.daos.{ClazzDAO, OfferDAO, RegistrationDAO, PartnerDAO}
import play.Play
import play.api.Logger
import play.api.Play.current
import play.api.cache.Cache
import play.api.i18n.MessagesApi
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
 * The basic application controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param socialProviderRegistry The social provider registry.
 */
@Singleton
class RegistrationController @Inject()(
                                       val messagesApi: MessagesApi,
                                       val env: Environment[Partner, JWTAuthenticator],
                                       socialProviderRegistry: SocialProviderRegistry,
                                       registrationDAO: RegistrationDAO)
  extends Silhouette[Partner, JWTAuthenticator] {


  def countByTrainee() = SecuredAction.async { implicit request =>
    registrationDAO.countByTrainee(request.identity.id.get).flatMap{ count =>
      Future.successful(Ok(Json.toJson(count)))
    }
  }

  def create() = SecuredAction.async(parse.json) { implicit request =>
    (request.body \ "idClazz").asOpt[String].map { idClazz =>
      registrationDAO.save(Registration(None, request.identity.id.get, UUID.fromString(idClazz)))
        .onFailure { case t => Logger.warn(t.getMessage) }
      Future.successful(Ok)
    }.getOrElse {
      Future.successful(BadRequest("Missing parameter [idClazz]"))
    }
  }

  def delete(idRegistration: String) = SecuredAction.async { implicit request =>
      registrationDAO.delete(UUID.fromString(idRegistration))
        .onFailure { case t => Logger.warn(t.getMessage) }
      Future.successful(Ok)
  }

}
