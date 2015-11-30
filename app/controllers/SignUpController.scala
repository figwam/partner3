package controllers

import java.sql.Timestamp
import java.util.{GregorianCalendar, UUID}
import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.{SignUpForm}
import models.daos.PartnerDAO
import models.{Studio, Address, Partner}
import models.services.PartnerService
import play.api.i18n.{ MessagesApi }
import play.api.libs.json._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc.Action

import scala.concurrent.Future

/**
 * The sign up controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param partnerService The partner service implementation.
 * @param authInfoRepository The auth info repository implementation.
 * @param avatarService The avatar service implementation.
 * @param passwordHasher The password hasher implementation.
 */
class SignUpController @Inject() (
  val messagesApi: MessagesApi,
  val env: Environment[Partner, JWTAuthenticator],
  partnerService: PartnerService,
  partnerDAO: PartnerDAO,
  authInfoRepository: AuthInfoRepository,
  avatarService: AvatarService,
  passwordHasher: PasswordHasher)
  extends Silhouette[Partner, JWTAuthenticator] {

  /**
   * Registers a new partner.
   *
   * @return The result to display.
   */
  def signUp = Action.async(parse.json) { implicit request =>
    request.body.validate[SignUpForm.Data].map { data =>
      val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
      partnerService.retrieve(loginInfo).flatMap {
        case Some(partner) =>
          Future.successful(Unauthorized(Json.obj("message" -> "partner.exists")))
        case None =>
          val authInfo = passwordHasher.hash(data.password)
          val addr = Address(None, data.street, data.city, data.zip, data.state, "Switzerland")
          val partner = Partner(
            None,
            loginInfo = loginInfo,
            firstname = Some(data.firstname),
            lastname = Some(data.lastname),
            mobile = None,
            phone = None,
            email = Some(data.email),
            emailVerified = false,
            createdOn = new GregorianCalendar(),
            updatedOn = new GregorianCalendar(),
            ptoken = None,
            isActive = true,
            inactiveReason = None,
            username = Some (data.email),
            fullname = Some(data.firstname + " " + data.lastname),
            avatarurl = None,
            revenue = Some(BigDecimal(2.5)),
            address = addr,
            Studio(
              id=None,
              name="TODO Studioname", // TODO: Create Signup controller for partner login
              address=Address(None, data.street, data.city, data.zip, data.state, "Switzerland")
            )
          )
          for {
            avatar <- avatarService.retrieveURL(data.email)
            partner <- partnerService.save(partner.copy(avatarurl = avatar))
            authInfo <- authInfoRepository.add(loginInfo, authInfo)
            authenticator <- env.authenticatorService.create(loginInfo)
            token <- env.authenticatorService.init(authenticator)
          } yield {
            env.eventBus.publish(SignUpEvent(partner, request, request2Messages))
            env.eventBus.publish(LoginEvent(partner, request, request2Messages))
            Ok(Json.obj("token" -> token))
          }
      }
    }.recoverTotal {
      case error =>
        Future.successful(Unauthorized(Json.obj("message" -> "invalid.data", "detail" -> JsError.toJson(error))))
    }
  }
}
