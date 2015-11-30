package controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models._
import models.daos.RegistrationDAO
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * The basic photos controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param socialProviderRegistry The social provider registry.
 */
@Singleton
class PhotosController @Inject()(
                                       val messagesApi: MessagesApi,
                                       val env: Environment[Partner, JWTAuthenticator],
                                       socialProviderRegistry: SocialProviderRegistry,
                                       registrationDAO: RegistrationDAO)
  extends Silhouette[Partner, JWTAuthenticator] {



}
