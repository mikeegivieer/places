import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [User::class, Place::class, Category::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun placeDao(): PlaceDao
    abstract fun categoryDao(): CategoryDao
}
