import androidx.room.*

@Dao
interface PlaceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: Place)


    @Query("SELECT * FROM Place")
    suspend fun getAllPlaces(): List<Place>


    @Delete
    suspend fun deletePlace(place: Place)
}
