package forms

import play.api.libs.json._
import play.api.libs.functional.syntax._
import Reads._

/**
 * The form which provides additional Form Validation
 */
object FormValidator {

  case class ClazzDef(name: String, description: String)

  object ClazzDef {
    implicit val clazzDefReads = (
      (__ \ 'name).read[String](minLength[String](1)) and
      (__ \ 'description).read[String](minLength[String](1))
      )(ClazzDef.apply _)
  }
}
