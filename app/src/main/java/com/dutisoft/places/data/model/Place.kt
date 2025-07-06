import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["username"],
            childColumns = ["ownerUsername"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Place(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ownerUsername: String,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val categoryId: Int?, // referencia a Category
    val photoUri: String? = null,
    val isPrivate: Boolean = false
)
