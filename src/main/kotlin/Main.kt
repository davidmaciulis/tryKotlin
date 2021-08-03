fun main(args: Array<String>) {

    // config module is responsible for reading config file and mapping it into objects
    val config = Config()

    // AlphaVantage is a free stock price data API service. This module accesses the API
    val alphaVantage = AlphaVantage()

    // db module is responsible for connecting to PostgreSQL database, inserting, and querying data
    val db = DB(config) // dependency injection via composition

    // control module controls the application actions
    val control = Control(config, alphaVantage, db) // dependency injection via composition
    control.run() // starts the application
}