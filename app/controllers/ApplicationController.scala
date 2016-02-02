package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models._
import play.api.i18n.MessagesApi

import scala.concurrent.Future

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
                                       val env: Environment[User, JWTAuthenticator],
                                       socialProviderRegistry: SocialProviderRegistry,
                                       cService: ClazzService,
                                       oService: OfferService)
  extends Silhouette[User, JWTAuthenticator] {

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
