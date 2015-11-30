package controllers

import java.util.UUID
import java.util.concurrent.TimeoutException
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import forms.ClazzDefForm
import models._
import models.daos._
import play.api.Logger
import play.api.i18n.{Messages, Lang, MessagesApi}
import play.api.libs.json._
import play.api.mvc.BodyParsers.parse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * The basic clazz def controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param socialProviderRegistry The social provider registry.
 */
@Singleton
class ClazzDefinitionController @Inject()(
                                       val messagesApi: MessagesApi,
                                       val env: Environment[Partner, JWTAuthenticator],
                                       socialProviderRegistry: SocialProviderRegistry,
                                       clazzDefinitionDAO: ClazzDefinitionDAO)
  extends Silhouette[Partner, JWTAuthenticator] {


  def create() = SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[ClazzDefForm.Data] match {
      case error: JsError => {
        Future.successful(BadRequest(Json.obj("message" -> Messages("save.fail"), "detail" -> JsError.toJson(error))))
      }
      case s: JsSuccess[ClazzDefForm.Data] => {
        request.body.validate[ClazzDefinition].map { clazzDef =>

          // In case of onetime set the activTill to class endDate, so it will be automatically historized
          val clazzDefCopy = clazzDef.recurrence match {
            case Recurrence.onetime => clazzDef.copy(idStudio = request.identity.studio.id, activeTill = clazzDef.endAt)
            case _ => clazzDef.copy(idStudio = request.identity.studio.id)
          }

          clazzDefinitionDAO.create(clazzDefCopy).flatMap {
            case c:ClazzDefinition =>
              Future.successful(Ok(Json.obj("message" -> Messages("save.ok"))))
            case _ =>
              Future.successful(BadRequest(Json.obj("message" -> Messages("save.fail"))))
          }
        }.recoverTotal {
          case error =>
            Future.successful(BadRequest(Json.obj("message" -> "invalid.data", "detail" -> JsError.toJson(error))))
        }
      }
    }
  }

  def listByPartner(page: Int, orderBy: Int, filter: String) = SecuredAction.async { implicit request =>
    clazzDefinitionDAO.listByPartner(page, 10, orderBy, request.identity.id.getOrElse(UUID.randomUUID())).flatMap { pageClazzes =>
      Future.successful(Ok(Json.toJson(pageClazzes)))
    }.recover {
      case ex: TimeoutException =>
        Logger.error("Problem found in clazz list process")
        InternalServerError(ex.getMessage)
    }
  }

}
