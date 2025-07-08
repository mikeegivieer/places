import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Place::class, Category::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
    abstract fun categoryDao(): CategoryDao
}
