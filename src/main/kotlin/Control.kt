
class Control(private val config: Config, // dependency injection via composition
              private val alphaVantage: AlphaVantage,
              private val database: DB) {

    fun run () { // method that controls the application
        val (url) = config.getAlphaVantageConfig() // gets the API url from the config file
        val json = alphaVantage.getJson(url) // downloads the json text file from API
        database.connect(); // connects to PostgreSQL database
        database.insertJson(json) // inserts the JSON file into the database
        database.getWvap() // queries the data from the database and calculates WVAP
    }
}