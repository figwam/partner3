package workers

import java.sql.Timestamp
import java.util.UUID
import javax.inject.{Inject, Singleton}

import akka.actor.{Props, Actor, Cancellable}
import models.{AppLogger, Clazz}
import models.daos.{LoggerDAO, ClazzDAO}
import play.Logger

import scala.concurrent.ExecutionContext.Implicits.global


/**
 * Created by alex on 27/09/15.
 */
@Singleton
class DBLogAdmin @Inject() (loggerDAO: LoggerDAO)  extends Actor {

  def receive = {
    case log: AppLogger =>
      try {
        loggerDAO.insert(log)
      } catch {
        case t: Throwable =>
          Logger.error("Log could not be written", t)
      }
  }

}
