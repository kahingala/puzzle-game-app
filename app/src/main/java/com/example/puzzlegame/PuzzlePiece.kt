
import android.content.Context
import androidx.appcompat.widget.AppCompatImageView

class PuzzlePiece(context: Context) : AppCompatImageView(context) {

    var originalPositionX: Int = 0
    var originalPositionY: Int = 0
    var currentPositionX: Int = 0
    var currentPositionY: Int = 0
    var isCorrectPosition: Boolean = false
    var canMove: Boolean = true  // Initialize canMove as true by default

    // Other properties and methods specific to PuzzlePiece
}
