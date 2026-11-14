import com.google.inject.AbstractModule
import repositories._

// Wires each repository trait to its implementation. Swap a line here to change storage.
class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[PlantRepository]).to(classOf[JsonFilePlantRepository])
    bind(classOf[ClimateRepository]).to(classOf[JsonFileClimateRepository])
    bind(classOf[GardenJobRepository]).to(classOf[JsonFileGardenJobRepository])
    bind(classOf[ActionColourRepository]).to(classOf[JsonFileActionColourRepository])
    bind(classOf[UserRepository]).to(classOf[JsonFileUserRepository])
    bind(classOf[UserGrowSettingsRepository]).to(classOf[JsonFileUserGrowSettingsRepository])
    bind(classOf[GardenMonthRepository]).to(classOf[JsonFileGardenMonthRepository])
    () // bind returns a builder we don't need
  }
}
