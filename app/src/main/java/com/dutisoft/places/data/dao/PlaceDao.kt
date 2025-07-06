import androidx.room.*

@Dao
interface PlaceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: Place)

    @Query("SELECT * FROM Place WHERE ownerUsername = :username")
    suspend fun getPlacesByUser(username: String): List<Place>

    @Delete
    suspend fun deletePlace(place: Place)
}
