package forms

import java.util.Date

import models.Recurrence.Recurrence
import models.Type.Type
import play.api.libs.json._
import play.api.libs.functional.syntax._
import Reads._

/**
 * The form which handles the clazzdef modification process.
 */
object ClazzDefForm {


  case class Data(
                    name: String,
                    description: String,
                    contingent: Short,
                    startFrom: Date,
                    tags: Type,
                    recurrence: Recurrence,
                    amount: BigDecimal)

  case class DataAddress( street: String,
                    city: String,
                    zip: String)


  object Data {
    import utils.Utils.Implicits._
    implicit val clazzDefReads = (
      (__ \ 'name).read[String](minLength[String](1)) and
      (__ \ 'description).read[String](minLength[String](1)) and
      (__ \ 'contingent).read[Short] and
      (__ \ 'startFrom).read[Date] and
      (__ \ 'tags).read[Type]and
      (__ \ 'recurrence).read[Recurrence] and
      (__ \ 'amount).read[BigDecimal]
      )(ClazzDefForm.Data.apply _)
  }
}
