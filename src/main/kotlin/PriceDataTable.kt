import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.time.LocalDateTime

class PriceDataTable : Table() { // an object that represents a relational table
    val timestamp: Column<LocalDateTime> = datetime("timestamp")
    val halfhourts: Column<LocalDateTime> = datetime("halfhourts")
    val open: Column<Float> = float("open")
    val high: Column<Float> = float("high")
    val low: Column<Float> = float("low")
    val close: Column<Float> = float("close")
    val volume: Column<Float> = float("volume")
}