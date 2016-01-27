package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordHasher}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.{SignUpForm}
import models._
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json._
import utils.{MailTokenService, MailService, FormValidator}
import views.html.mails
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc.Action

import scala.concurrent.Future

/**
  * The sign up controller.
  *
  * @param messagesApi The Play messages API.
  * @param env The Silhouette environment.
  * @param authInfoRepository The auth info repository implementation.
  * @param avatarService The avatar service implementation.
  * @param passwordHasher The password hasher implementation.
  */
class SignUpController @Inject()(
                                  val messagesApi: MessagesApi,
                                  val env: Environment[User, JWTAuthenticator],
                                  service: PartnerService,
                                  authInfoRepository: AuthInfoRepository,
                                  avatarService: AvatarService,
                                  passwordHasher: PasswordHasher,
                                  credentialsProvider: CredentialsProvider,
                                  val ms: MailService,
                                  val tokenService: MailTokenService[MailTokenUser])
  extends Silhouette[User, JWTAuthenticator] {

  lazy val isDev: Boolean = play.api.Play.isDev(play.api.Play.current)

  /**
    * Registers a new partner.
    *
    * @return The result to display.
    */
  def signUp = Action.async(parse.json) { implicit request =>
    request.body.validate[SignUpForm.Data].map { data =>
      val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
      service.retrieve(loginInfo).flatMap {
        case Some(partner) =>
          Future.successful(Unauthorized(Json.obj("message" -> "partner.exists")))
        case None =>
          val mailtoken = MailTokenUser(data.email, isSignUp = true)
          val link = routes.SignUpController.signUpConfirm(mailtoken.id).absoluteURL()
          val authInfo = passwordHasher.hash(data.password)
          val addr = Address(None, data.street, data.city, data.zip, data.state, "Switzerland")

          val partner = Partner(
            firstname = Some(data.firstname),
            lastname = Some(data.lastname),
            email = Some(data.email),
            username = Some(data.email),
            fullname = Some(data.firstname + " " + data.lastname),
            revenue = Some(BigDecimal(2.5))
          )

          for {
            avatar <- avatarService.retrieveURL(data.email)
            partner <- service.signUp(partner.copy(avatarurl = avatar),loginInfo, addr)
            authInfo <- authInfoRepository.add(loginInfo, authInfo)
            authenticator <- env.authenticatorService.create(loginInfo)
            logintoken <- env.authenticatorService.init(authenticator)
          } yield {
            env.eventBus.publish(SignUpEvent(partner, request, request2Messages))
            env.eventBus.publish(LoginEvent(partner, request, request2Messages))
            ms.sendEmailAsync(data.email)(
              subject = Messages("mail.welcome.subject"),
              bodyHtml = mails.welcome(data.firstname, link).toString(),
              bodyText = mails.welcomeTxt(data.firstname, link).toString()
            )
            if (isDev) Ok(Json.obj("link" -> link, "token" -> mailtoken.id))
            else Ok(Json.obj("token" -> logintoken))
          }
      }
    }.recoverTotal {
      case error =>
        Future.successful(Unauthorized(Json.obj("message" -> "invalid.data", "detail" -> JsError.toJson(error))))
    }
  }

  def signUpConfirm(tokenId: String) = Action.async { implicit request =>
    tokenService.retrieve(tokenId).flatMap {
      case Some(token) if token.isSignUp && !token.isExpired => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, token.email)
        service.retrieve(loginInfo).flatMap {
          case Some(user) => { user match {
            case t:Partner => {
              service.update(t.copy(emailVerified = Some(true))).flatMap {
                case obj:Partner =>
                  Future.successful(Ok(Json.obj("message" -> Messages("email.verify.success"))))
                case _ =>
                  logger.error("Email verification failed")
                  Future.successful(InternalServerError(Json.obj("message" -> Messages("email.verify.fail"))))
              }
            }
          }
          }
          case None => Future.successful(Ok(Json.obj("message" -> "trainee.not.exists")))
        }
      }
      case Some(token) => {
        tokenService.consume(tokenId)
        Future.successful(Ok(Json.obj("message" -> "trainee.not.exists")))
      }
      case None => Future.successful(Ok(Json.obj("message" -> "trainee.not.exists")))
    }
  }

  def forgotPassword = Action.async(parse.json) { implicit request =>
    request.body.validate[FormValidator.Email] match {
      case error: JsError => {
        Future.successful(BadRequest(Json.obj("message" -> Messages("invalid.data"), "detail" -> JsError.toJson(error))))
      }
      case o: JsSuccess[FormValidator.Email] => {
        request.body.validate[FormValidator.Email].map { obj =>
          val loginInfo = LoginInfo(CredentialsProvider.ID, obj.email)
          service.retrieve(loginInfo).flatMap {
            case Some(_) => {
              val token = MailTokenUser(obj.email, isSignUp = false)
              val link = routes.SignUpController.resetPassword(token.id).absoluteURL()
              tokenService.create(token).map { _ =>
                ms.sendEmailAsync(obj.email)(
                  subject = Messages("mail.forgot.password.subject"),
                  bodyHtml = mails.forgotPassword(obj.email, link)+"",
                  bodyText = mails.forgotPasswordTxt(obj.email, link)+""
                )
              }
              if (isDev) Future.successful(Ok(Json.obj("link" -> link, "token" -> token.id)))
              else Future.successful(Ok(Json.obj("message" -> "OK")))
            }
            case None => Future.successful(Ok(Json.obj("message" -> "trainee.not.exists")))
          }
        }.recoverTotal {
          case error =>
            Future.successful(BadRequest(Json.obj("message" -> "invalid.data", "detail" -> JsError.toJson(error))))
        }
      }
    }
  }

  def resetPassword(tokenId: String) = Action.async(parse.json) { implicit request =>
    tokenService.retrieve(tokenId).flatMap {
      case Some(token) if (!token.isSignUp && !token.isExpired) => {
        request.body.validate[FormValidator.PasswordReset] match {
          case error: JsError => {
            Future.successful(BadRequest(Json.obj("message" -> Messages("invalid.data"), "detail" -> JsError.toJson(error))))
          }
          case o: JsSuccess[FormValidator.PasswordReset] => {
            request.body.validate[FormValidator.PasswordReset].map { obj =>
              if (!obj.password1.equals(obj.password2))
                Future.successful(BadRequest(Json.obj("message" -> Messages("password.not.match"))))
              else {
                val loginInfo = LoginInfo(CredentialsProvider.ID, token.email)
                service.retrieve(loginInfo).flatMap {
                  case Some(user) => {
                    val authInfo = passwordHasher.hash(obj.password1)
                    for {
                      authInfo <- authInfoRepository.update(loginInfo, authInfo)
                      authenticator <- env.authenticatorService.create(loginInfo)
                      result <- env.authenticatorService.renew(authenticator)
                    } yield {
                      env.eventBus.publish(LoginEvent(user, request, request2Messages))
                      Ok(Json.obj("message" -> "OK"))
                    }
                  }
                  case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
                }
              }
            }.recoverTotal {
              case error =>
                Future.successful(BadRequest(Json.obj("message" -> "invalid.data", "detail" -> JsError.toJson(error))))
            }

          }
        }
      }
      case Some(token) => {
        tokenService.consume(tokenId)
        Future.successful(NotFound)
      }
      case None => Future.successful(NotFound)
    }
  }

  /**
    * Saves the new password and renew the cookie
    */
  def changePassword = SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[FormValidator.PasswordChange] match {
      case error: JsError => {
        Future.successful(BadRequest(Json.obj("message" -> Messages("invalid.data"), "detail" -> JsError.toJson(error))))
      }
      case o: JsSuccess[FormValidator.PasswordChange] => {
        request.body.validate[FormValidator.PasswordChange].map { obj =>
          if (!obj.password1.equals(obj.password2))
            Future.successful(BadRequest(Json.obj("message" -> Messages("password.not.match"))))
          else {
            val authInfo = passwordHasher.hash(obj.password1)
            credentialsProvider.authenticate(Credentials(request.identity.email.get, obj.current)).flatMap { loginInfo =>
              for {
                _ <- authInfoRepository.update(loginInfo, authInfo)
                authenticator <- env.authenticatorService.create(loginInfo)
                result <- env.authenticatorService.renew(authenticator)
              } yield {
                Ok(Json.obj("message" -> "OK"))
              }
            }.recover {
              case e: ProviderException => BadRequest(Json.obj("message" -> Messages("password.incorrect")))
            }
          }
        }.recoverTotal {
          case error =>
            Future.successful(BadRequest(Json.obj("message" -> "invalid.data", "detail" -> JsError.toJson(error))))
        }

      }
    }
  }
}
