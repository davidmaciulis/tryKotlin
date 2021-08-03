
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.io.File

class Config {
    private val FILEPATH = "./src/main/kotlin/config.json" // path to config file
    private val jsonText = File(FILEPATH).readText(Charsets.UTF_8) // reads the config file from disk
    private val jsonObj = Json.parseToJsonElement(jsonText).jsonObject // maps json string to json object

    private val dbConfigJson = jsonObj["database"] // accesses "database" config object
    private val dbConfig = Json.decodeFromString<DbConfig>(dbConfigJson.toString()) // maps json to data class

    fun getDbConfig(): DbConfig { // database config data class accessor
        return dbConfig
    }

    private val avConfigJson = jsonObj["alphavantage"] // accesses "alphavantage" config object
    private val avConfig = Json.decodeFromString<AvConfig>(avConfigJson.toString()) // maps json to data class

    fun getAlphaVantageConfig() : AvConfig { // alphavantage config data class accessor
        return avConfig
    }

    @Serializable
    data class DbConfig ( // data class holding database config information
        val url: String,
        val driver: String,
        val user: String,
        val password: String
    )

    @Serializable
    data class AvConfig ( // data class holding alphavantage config information
        val url: String
    )
}


