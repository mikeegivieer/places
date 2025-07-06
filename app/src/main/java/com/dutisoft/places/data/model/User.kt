import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey val username: String,
    val avatar: String? = null // Puede ser null si no tiene foto
)
