package modules

import com.google.inject.AbstractModule
import models._
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport
import workers.{ClazzScheduler, DBLogAdmin}

/**
 * Created by alex on 28/09/15.
 */
class AppModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  /**
   * Configures the module.
   */
  def configure() {
    bind[UserService].to[PartnerService]
    bind[AddressService].to[AddressServicePartnerImpl]


    bindActor[ClazzScheduler]("ClazzScheduler")
    bindActor[DBLogAdmin]("DBLogAdmin")
  }
}
