package repositories

import com.google.inject.{Inject, Singleton}
import org.mongodb.scala.{MongoClient, MongoDatabase}
import play.api.Configuration

@Singleton
class MongoConnection @Inject()(config: Configuration) {
    private val mongoUri: String = config.get[String]("mongodb.uri")

    val client: MongoClient = MongoClient(mongoUri)
    val database: MongoDatabase = client.getDatabase("gardening-dashboard")

    def close(): Unit = client.close()
}