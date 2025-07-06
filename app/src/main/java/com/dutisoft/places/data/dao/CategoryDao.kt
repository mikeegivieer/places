import androidx.room.*

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Query("SELECT * FROM Category")
    suspend fun getAllCategories(): List<Category>

    @Query("SELECT * FROM Category WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?
}
