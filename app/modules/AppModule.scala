package modules

import com.google.inject.AbstractModule
import models.{PartnerService, PartnerServiceImpl, UserService}
import models.daos._
import net.codingwell.scalaguice.ScalaModule

/**
 * Created by alex on 28/09/15.
 */
class AppModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  def configure() {
    bind[UserService].to[PartnerService]
  }
}
