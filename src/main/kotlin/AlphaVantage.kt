import java.net.URL
import kotlin.io.*

class AlphaVantage() {
    fun getJson(url : String): String {
        return URL(url).readText() // download json file from an API
    }
}