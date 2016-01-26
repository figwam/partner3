package models

import java.util.UUID
import javax.inject.Inject

import models.daos.DAOSlick
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * Partner implementation of the Address ressource. It does not check if the address belongs to user or not.
  *
  * @param dbConfigProvider
  */
class AddressServicePartnerImpl @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider)
  extends AddressServiceImpl(dbConfigProvider: DatabaseConfigProvider) with DAOSlick {

  import driver.api._

  override def retrieveByOwner(id: UUID, owner: UUID): Future[Option[Address]] = {
    db.run(slickAddresses.filter(_.id === id).join(slickPartners.filter(_.id === owner)).on(_.id === _.idAddress).result.headOption)
      .map(obj => obj.map(o => o match {
        case (a: DBAddress, t: DBPartner) => entity2model(a)
      }))
  }

}
