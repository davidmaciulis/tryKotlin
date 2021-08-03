import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.times
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.serialization.json.*
import java.time.LocalTime

class DB (private val config: Config) {

    // Kotlin Exposed object that represents a relation
    private val priceDataTable = PriceDataTable()

    // maps json keys to corresponding Table column objects
    private val priceDataJsonMap = mapOf(
        "1. open" to priceDataTable.open,
        "2. high" to priceDataTable.high,
        "3. low" to priceDataTable.low,
        "4. close" to priceDataTable.close,
        "5. volume" to priceDataTable.volume)

    // date and time java formatter objects
    private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
    private val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    // java time objects representing start and end of a trading day
    private val startTime = LocalTime.parse("09:35:00", timeFormat)
    private val endTime = LocalTime.parse("16:00:00", timeFormat)

    fun connect() { // connects to the database
        val (url, driver, user, password) = config.getDbConfig()
        val db = Database.connect(url, driver, user, password)
        println("Connected to db: ${db.name}")
    }

    fun insertJson(jsonString: String) { // parses and inserts json string into the database
        transaction () {

            val element = Json.parseToJsonElement(jsonString) // Kotlin library json parser

            // parses lastRefreshed timestamp
            val metaData = element.jsonObject.values.first().jsonObject
            val lastRefreshedStr = metaData["3. Last Refreshed"]!!.jsonPrimitive.content
            val lastRefreshed = LocalDateTime.parse(lastRefreshedStr, dateFormat).toLocalDate()

            var fiveMinuteIncCounter = 0 // counts 5 min timeseries increments

            // timestamp holding 30min window timestamp to be assigned to a row
            var halfHourTs = LocalDateTime.of(lastRefreshed, endTime)

            val timeSeries = element.jsonObject.values.last().jsonObject // gets timeseries object from json

            for ((time, series) in timeSeries) { // iterates over every element in the json object
                val priceValues = series.jsonObject

                // current row data and time
                val currentTimestamp = LocalDateTime.parse(time, dateFormat)
                val currentDate = currentTimestamp.toLocalDate()
                val currentTime = currentTimestamp.toLocalTime()

                // filters out data that is not from the latest trading day
                // and that does not fall within normal trading hours
                if (lastRefreshed == currentDate
                    && currentTime >= startTime
                    && currentTime <= endTime ) {

                    // tracks which timestamps should fall within a 30min window
                    if (fiveMinuteIncCounter==6) {
                        halfHourTs = halfHourTs.minusMinutes(30)
                        fiveMinuteIncCounter = 0 // reset the counter every 30min (5min x 6)
                    }
                    fiveMinuteIncCounter++

                    priceDataTable.insert { // inserts the current row into the database relation
                        it[timestamp] = currentTimestamp // current timestamp
                        it[halfhourts] = halfHourTs // current 30min window

                        for (key in priceValues.keys) { // inserts close, high, low, open, volume data
                            val column = priceDataJsonMap[key] // mapping string keys to Table column objects
                            it[column!!] = priceValues[key]!!.jsonPrimitive?.float
                        }
                    }
                }
            }
        }
    }

    fun getWvap () { // query data and calculate WVAP
        transaction {
            val priceVolume = priceDataTable.close * (priceDataTable.volume as Column<Float>) // multiplies two columns

            (priceDataTable.slice(priceDataTable.halfhourts, // selecting relevant columns
                priceVolume.sum().alias("priceVolumeSum"), // sums price and volume product in each row in the 30min window
                priceDataTable.volume.sum().alias("avgVolume")) // sums volume in the 30min window
            .selectAll()
            .groupBy(priceDataTable.halfhourts)) // groups the rows into 30min windows
            .orderBy(priceDataTable.halfhourts to SortOrder.ASC) // orders the rows
            .forEach { // iterates through each row to do the final calculation and print out the results
                val halfhourts = it[priceDataTable.halfhourts]

                val priceVolumeSum = it[priceVolume.sum().alias("priceVolumeSum")]
                val avgVolume = it[priceDataTable.volume.sum().alias("avgVolume")]
                val wvap = priceVolumeSum?.div(avgVolume!!) // dividing the sum of price and volume product by the sum of volume

                println("$halfhourts $priceVolumeSum $avgVolume $wvap")
            }
        }
    }
}

