package forms

//import play.api.data.Form
//import play.api.data.Forms._
//import play.api.data._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import Reads._
/**
 * The form which handles the sign up process.
 */
object SignUpForm {

  /**
   * A play framework form.
   */
  /*
  val form = Form(
    mapping(
      "firstname" -> nonEmptyText,
      "lastname" -> nonEmptyText,
      "email" -> email,
      "password" -> nonEmptyText,
      "street" -> nonEmptyText,
      "city" -> nonEmptyText,
      "zip" -> nonEmptyText.verifying("falsche Eingabe", {_.matches("d{4,4}")}),
      "state" -> nonEmptyText,
      "aboId" -> longNumber(1)
    )(Data.apply)(Data.unapply)
  )
  */

  /**
   * The form data.
   *
   * @param firstname The first name of a trainee.
   * @param lastname The last name of a trainee.
   * @param email The email of the trainee.
   * @param password The password of the trainee.
   */
  case class Data(
    firstname: String,
    lastname: String,
    email: String,
    password: String,
                   street: String,
                   city: String,
                   zip: String,
                   state: String)

  /**
   * The companion object.
   */
  object Data {

    /**
     * Converts the [Date] object to Json and vice versa.
     */
    implicit val dataReads = (
      (__ \ 'firstname).read[String](minLength[String](1)) and
      (__ \ 'lastname).read[String](minLength[String](1)) and
      (__ \ 'email).read[String](email) and
      (__ \ 'password).read[String](minLength[String](1)) and
      (__ \ 'street).read[String](minLength[String](1)) and
      (__ \ 'city).read[String](minLength[String](1)) and
      (__ \ 'zip).read[String](verifying[String](_.matches("\\d{4,4}"))) and
      (__ \ 'state).read[String]
    )(SignUpForm.Data.apply _)
  }
}
