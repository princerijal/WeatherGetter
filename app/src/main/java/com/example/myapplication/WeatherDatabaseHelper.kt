import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class WeatherDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "WeatherDB"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "SavedWeather"
        const val COLUMN_ID = "id"
        const val COLUMN_LOCATION = "location"
        const val COLUMN_COUNTRY = "country"
        const val COLUMN_TEMPERATURE = "temperature"
        const val COLUMN_CONDITION = "condition"
        const val COLUMN_ICON_URL = "icon_url"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_LOCATION TEXT,
                $COLUMN_COUNTRY TEXT,
                $COLUMN_TEMPERATURE TEXT,
                $COLUMN_CONDITION TEXT,
                $COLUMN_ICON_URL TEXT
            )
        """
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Insert a new weather entry into the database
    fun insertWeather(location: String, country: String, temperature: String, condition: String, iconUrl: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_LOCATION, location)
            put(COLUMN_COUNTRY, country)
            put(COLUMN_TEMPERATURE, temperature)
            put(COLUMN_CONDITION, condition)
            put(COLUMN_ICON_URL, iconUrl)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    // Retrieve all saved weather data
    fun getAllWeather(): List<SavedWeather> {
        val savedWeatherList = mutableListOf<SavedWeather>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                    val location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION))
                    val country = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COUNTRY))
                    val temperature = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEMPERATURE))
                    val condition = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONDITION))
                    val iconUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ICON_URL))

                    savedWeatherList.add(
                        SavedWeather(id, location, country, temperature, condition, iconUrl)
                    )
                } while (cursor.moveToNext())
            }
        } finally {
            cursor?.close()
        }

        return savedWeatherList
    }

    // Delete a weather entry by its ID
    fun deleteWeather(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_ID=?", arrayOf(id.toString()))
    }
}

// Data class for saved weather
data class SavedWeather(
    val id: Int,
    val location: String,
    val country: String,
    val temperature: String,
    val condition: String,
    val iconUrl: String
)
