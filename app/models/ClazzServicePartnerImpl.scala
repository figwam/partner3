package models

import java.sql.Timestamp
import java.util.UUID
import javax.inject.Inject

import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * Partner implementation of the Clazz ressource.
  *
  * @param dbConfigProvider
  */
class ClazzServicePartnerImpl @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider)
  extends ClazzServiceImpl(dbConfigProvider: DatabaseConfigProvider) with DAOSlick {

  import driver.api._



  override def listPersonalizedAll(page: Int = 0, pageSize: Int = 10, sortBy: Int = 1, filter: String = "%", idUser: UUID): Future[Page] = list(page, pageSize, sortBy,filter)


  override def listPersonalizedMy(page: Int = 0, pageSize: Int = 10, sortBy: Int = 1, filter: String = "%", idUser: UUID, startFrom: Timestamp, endAt: Timestamp): Future[Page] = {
    val offset = if (page > 0) pageSize * page else 0

    val action = (for {
      studio <- slickStudios.filter(_.idPartner === idUser)
      clazzDef <- slickClazzDefinitions.filter(_.idStudio === studio.id)
      clazz <- slickClazzes.filter(_.idClazzDef === clazzDef.id)
      clazzView <- slickClazzViews
        .sortBy(r => sortBy match {case 1 => r.startFrom case _ => r.startFrom})
        .filter(_.startFrom >= startFrom)
        .filter(_.endAt <= endAt)
        .filter(_.searchMeta.toLowerCase like filter.toLowerCase) if clazzView.id === clazz.id
      s <- slickStudios.filter(_.id === clazzView.idStudio)
      a <- slickAddresses.filter(_.id === s.idAddress)
    } yield (clazzView, s, a)).drop(offset).take(pageSize)
    val totalRows = countMy(filter, idUser, startFrom, endAt)


    val result = db.run(action.result)
    result.map { clazz =>
      clazz.map {
        // go through all the DBClazzes and map them to Clazz
        case (clazz, studio, addressS) => {
          entity2model(clazz, studio, addressS, None)
        }
      } // The result is Seq[Clazz] flapMap (works with Clazz) these to Page
    }.flatMap (c3 => totalRows.map (rows => Page(c3, page, offset.toLong, rows.toLong)))
  }

  private def countMy(filter: String, idUser: UUID, startFrom: Timestamp, endAt: Timestamp): Future[Int] = {
    val action = for {
      studio <- slickStudios.filter(_.idPartner === idUser)
      clazzDef <- slickClazzDefinitions.filter(_.idStudio === studio.id)
      clazz <- slickClazzes.filter(_.idClazzDef === clazzDef.id)
      clazzView <- slickClazzViews
        .filter(_.startFrom >= startFrom)
        .filter(_.endAt <= endAt)
        .filter(_.startFrom >= new Timestamp(System.currentTimeMillis()))
        .filter(_.searchMeta.toLowerCase like filter.toLowerCase) if clazzView.id === clazz.id
    } yield ()
    db.run(action.length.result)
  }
}
