import androidx.room.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM User WHERE username = :username")
    suspend fun getUser(username: String): User?
}
