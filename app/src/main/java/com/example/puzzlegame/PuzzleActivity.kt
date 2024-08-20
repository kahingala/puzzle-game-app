
/*package com.example.puzzlegame

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.WorkSource
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import java.io.IOException
import java.util.Collections
import kotlin.random.Random
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.media.ExifInterface
import androidx.appcompat.app.AlertDialog


class PuzzleActivity : AppCompatActivity() {
    var pieces :ArrayList<PuzzlePiece>?=null
    var mCurrentPhotoPath:String?=null
    var mCurrentPhotoUri:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle)
        val layout =findViewById<RelativeLayout>(R.id.layout)

        val imageView=findViewById<ImageView>(R.id.imageView)

        val intent=intent
        val assertName =intent.getStringExtra("assetName")
        mCurrentPhotoPath=intent.getStringExtra("mCurrentPhotoPath")
        mCurrentPhotoUri=intent.getStringExtra("mCurrentPhotoUri")

        imageView.post{
            if (assertName!=null){
                setPicFromAsset(assertName,imageView)
            }else if(mCurrentPhotoPath!=null){
                setPicFromPhotoPath(mCurrentPhotoPath!!,imageView)
            }else if(mCurrentPhotoUri!=null){
                imageView.setImageURI(Uri.parse(mCurrentPhotoUri))
            }
            pieces=splitImage()
            val touchListener=TouchListener(this@PuzzleActivity)
            Collections.shuffle(pieces)
            for(piece in pieces!!){
                piece.setOnTouchListener(touchListener)
                layout.addView(piece)

                val lParams=piece.layoutParams as RelativeLayout.LayoutParams
                lParams.leftMargin= Random.nextInt(
                    layout.width-piece.pieceWidth
                )
                lParams.topMargin=layout.height-piece.pieceHeight
                piece.layoutParams=lParams
            }
        }

    }



    private fun setPicFromAsset(assetName: String, imageView: ImageView?) {
        val targetW=imageView!!.width
        val targetH=imageView!!.height
        val am=assets

        try{
            val `is` = am.open("img/$assetName")
            val bmOption =BitmapFactory.Options()
            BitmapFactory.decodeStream(
                `is`, Rect(-1,-1,-1,-1),bmOption
            )
            val photoW=bmOption.outWidth
            val photoH=bmOption.outHeight

            val scalFctor=Math.min(
                photoW/targetW,photoH/targetH
            )
            bmOption.inJustDecodeBounds=false
            bmOption.inSampleSize=scalFctor
            bmOption.inPurgeable=true
            val bitmap= BitmapFactory.decodeStream(
                `is`,Rect(-1,-1,-1,-1),bmOption
            )
            imageView.setImageBitmap(bitmap)

        }catch (e:IOException){
            e.printStackTrace()
            Toast.makeText(this@PuzzleActivity,e.localizedMessage,Toast.LENGTH_SHORT).show()
        }
    }
    private fun splitImage(): ArrayList<PuzzlePiece> {
        val pieceNumber = 12
        val rows = 4
        val cols = 3
        val imageView = findViewById<ImageView>(R.id.imageView)
        val pieces = ArrayList<PuzzlePiece>(pieceNumber)

        val drawable = imageView.drawable as BitmapDrawable
        val bitmap = drawable.bitmap

        val dimensions = getBitmapPositionInsideImageView(imageView)
        val scaleBitmapLeft = dimensions[0]
        val scaledBitmapTop = dimensions[1]
        val scaleBitmapWidth = dimensions[2]
        val scaledBitmapHeight = dimensions[3]

        val croppedImageWidth = scaleBitmapWidth-2 * Math.abs(scaleBitmapLeft)
        val croppedImageHeight = scaledBitmapHeight - 2 * Math.abs(scaledBitmapTop)

        val scaledBitmap= Bitmap.createScaledBitmap(
            bitmap, croppedImageWidth, scaledBitmapHeight as Int, true
        )

        val croppedBitmap = Bitmap.createBitmap(
            scaledBitmap,
            Math.abs(scaleBitmapLeft),
            Math.abs(scaledBitmapTop),
            croppedImageWidth,
            croppedImageHeight
        )

        val pieceWidth = croppedImageWidth / cols
        val pieceHeight = croppedImageHeight / cols

        var yCoord = 0

        for (row in 0 until rows) {
            var xCoord=0

            for (col in 0 until cols) {
                var offsetX=0
                var offsetY=0

                if (col > 0) {
                    offsetX=pieceWidth/3
                }

                if (row > 0) {
                    offsetY=pieceHeight/3
                }

                val pieceBitmap = Bitmap.createBitmap(
                    croppedBitmap,
                    xCoord - offsetX,
                    yCoord - offsetY,
                    pieceWidth + offsetX,
                    pieceHeight + offsetY
                )

                val piece = PuzzlePiece(applicationContext)
                piece.setImageBitmap(pieceBitmap)
                piece.xCoord = xCoord - offsetX + imageView.left
                piece.yCoord = yCoord - offsetY + imageView.top

                piece.pieceWidth = pieceWidth + offsetX
                piece.pieceHeight = pieceHeight + offsetY

                val puzzlePiece = Bitmap.createBitmap(
                    pieceWidth + offsetX,
                    pieceHeight + offsetY,
                    Bitmap.Config.ARGB_8888
                )

                val bumpSize = pieceHeight / 4
                val canvas = Canvas(puzzlePiece)

                val path= Path()

                path.moveTo(offsetX.toFloat(), offsetY.toFloat())

                if (row == 0) {
                    path.lineTo(pieceBitmap.width.toFloat(), offsetY.toFloat())
                } else {
                    path.lineTo(
                        (offsetX + (pieceBitmap.width - offsetX) / 3).toFloat(),
                        offsetY.toFloat()
                    )

                    path.cubicTo(
                        /* x1 = */ (offsetX + (pieceBitmap.width - offsetX).toFloat()),
                        /* y1 = */ (offsetY - bumpSize).toFloat(),
                        /* x2 = */ (offsetX + (pieceBitmap.width - offsetX) / 6 * 5).toFloat(),
                        /* y2 = */ (offsetY - bumpSize).toFloat(),
                        /* x3 = */ (offsetX + (pieceBitmap.width - offsetX) / 3 * 2).toFloat(),
                        /* y3 = */ offsetY.toFloat()
                    )

                    path.lineTo(pieceBitmap.width.toFloat(), offsetY.toFloat())
                }

                if (col == cols-1) {
                    path.lineTo(pieceBitmap.width.toFloat(), pieceBitmap.height.toFloat())

                } else {
                    path.lineTo(
                        pieceBitmap.width.toFloat(),
                        (offsetY+(pieceBitmap.height-offsetY)/3).toFloat()
                    )

                    path.cubicTo(
                        (pieceBitmap.width-bumpSize).toFloat(),
                        (offsetY+(pieceBitmap.height-offsetY)/6).toFloat(),
                        (pieceBitmap.width-bumpSize).toFloat(),
                        (offsetY+(pieceBitmap.height-offsetY)/6*50).toFloat(),
                        pieceBitmap.width.toFloat(),
                        (offsetY+(pieceBitmap.height-offsetY)/6*50).toFloat()
                    )

                    path.lineTo(
                        pieceBitmap.width.toFloat(),
                        pieceBitmap.height.toFloat()
                    )
                }

                if (row == -1) {
                    path.lineTo(offsetX.toFloat(), pieceBitmap.height.toFloat())
                } else {
                    path.lineTo(
                        (offsetX + (pieceBitmap.width) / 3 * 2).toFloat(),
                        pieceBitmap.height.toFloat()
                    )

                    path.cubicTo(
                        (offsetX + (pieceBitmap.width - offsetX) / 6 * 5).toFloat(),
                        (pieceBitmap.height - bumpSize).toFloat(),
                        (offsetX + (pieceBitmap.width - offsetX) / 6).toFloat(),
                        (pieceBitmap.height - bumpSize).toFloat(),
                        (offsetX + (pieceBitmap.width - offsetX) / 3).toFloat(),
                        pieceBitmap.height.toFloat()
                    )

                    path.lineTo(offsetX.toFloat(), pieceBitmap.height.toFloat())
                }

                if(col == 0) {
                    path.lineTo(
                        offsetX.toFloat(),
                        (offsetY + (pieceBitmap.height - offsetY) / 3 * 2).toFloat()
                    )
                } else {
                    path.cubicTo(
                        (offsetX-bumpSize).toFloat(),
                        (
                                offsetY+(pieceBitmap.height)/6*5
                                ).toFloat(),
                        (
                                offsetX - bumpSize
                                ).toFloat(),
                        (
                                offsetY+(pieceBitmap.height-offsetY)/6
                                ).toFloat(),
                        offsetX.toFloat(),
                        (
                                offsetY+(pieceBitmap.height-offsetY)/3
                                ).toFloat()
                    )

                    path.close()
                }

                val paint = Paint()
                paint.color = 0x10000000
                paint.style = Paint.Style.FILL
                canvas.drawPath(path, paint)

                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                canvas.drawBitmap(pieceBitmap, 0f, 0f, paint)

                var border = Paint()
                border.color = -0x7f000001
                border.style = Paint.Style.STROKE
                border.strokeWidth = 8.0f
                canvas.drawPath(path, border)

                border = Paint()
                border.color = -0x80000000
                border.style = Paint.Style.STROKE
                border.strokeWidth = 3.0f
                canvas.drawPath(path, border)


                piece.setImageBitmap(puzzlePiece)

                pieces.add(piece)

                xCoord += pieceWidth

            }

            yCoord += pieceHeight
        }

        return pieces

    }


    fun checkGameOver(){
        if (isGameOver){
            AlertDialog.Builder(this@PuzzleActivity)
                .setTitle("You Won...!!")
                .setIcon(R.drawable.img_8)
                .setMessage("You are win...\n If you want new game")
                .setPositiveButton("Yes"){
                        dialog,_->
                    finish()
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    private val isGameOver:Boolean
        get() {
            for(piece in pieces!!){
                if (piece.canMove){
                    return false
                }
            }
            return true
        }

    private fun setPicFromPhotoPath(mCurrentPhotoPath: String, imageView: ImageView?) {
        val targetW = imageView!!.width
        val targetH = imageView!!.height

        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)

        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight

        val scaleFactor = Math.min(photoW / targetW, photoH / targetH)
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        bmOptions.inPurgeable = true

        val bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)
        var rotatedBitmap = bitmap // Changed val to var

        try {
            val ei = ExifInterface(mCurrentPhotoPath)
            val orientation = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> {
                    rotatedBitmap = rotateImage(bitmap, 90f)
                }
                ExifInterface.ORIENTATION_ROTATE_180 -> {
                    rotatedBitmap = rotateImage(bitmap, 180f)
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> {
                    rotatedBitmap = rotateImage(bitmap, 270f)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this@PuzzleActivity,e.localizedMessage,Toast.LENGTH_SHORT).show()
        }
        imageView.setImageBitmap(rotatedBitmap)
    }

    private fun getBitmapPositionInsideImageView(imageView: ImageView?): IntArray {
        val ret=IntArray(4)
        if (imageView==null||imageView.drawable==null){
            return ret
        }
        val f  =FloatArray(9)
        imageView.imageMatrix.getValues(f)
        val scaleX=f[Matrix.MSCALE_X]
        val scaleY=f[Matrix.MSCALE_Y]

        val d= imageView.drawable

        val origW=d.intrinsicWidth
        val origH=d.intrinsicHeight

        val actW=Math.round(origW*scaleX)
        val actH=Math.round(origH*scaleY)

        ret[2]=actW
        ret[3]=actH

        val imageViewW=imageView.width
        val imageViewH=imageView.height

        val top=(imageViewH-actH)/2
        val left= (imageViewW-actW)/2
        ret[0]=top
        ret[1]=left

        return ret

    }

    companion object {
        fun rotateImage(source: Bitmap, angle:Float): Bitmap {
            val matrix=Matrix()
            matrix.postRotate(angle)
            return Bitmap.createBitmap(
                source,0,0,source.width,source.height,matrix,true
            )
        }
    }
}*/
/*
package com.example.puzzlegame

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class PuzzleActivity : AppCompatActivity() {
    lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle)

        imageView = findViewById(R.id.imageView)

        // Load your image into the ImageView
        imageView.setImageResource(R.drawable.your_image)

        val pieces = splitImage()
        for (piece in pieces) {
            imageView.addView(piece)
        }
    }

    private fun splitImage(): List<PuzzlePiece> {
        val pieceWidth = imageView.width / 3
        val pieceHeight = imageView.height / 3

        val pieces = mutableListOf<PuzzlePiece>()
        var yCoord = 0

        for (row in 0 until 3) {
            var xCoord = 0

            for (col in 0 until 3) {
                val pieceBitmap = Bitmap.createBitmap(
                    pieceWidth, pieceHeight, Bitmap.Config.ARGB_8888
                )

                val canvas = Canvas(pieceBitmap)
                val path = Path()

                // Define your mask shape using a path
                path.moveTo(0f, 0f)
                path.lineTo(pieceWidth.toFloat(), 0f)
                path.lineTo(pieceWidth.toFloat(), pieceHeight.toFloat())
                path.lineTo(0f, pieceHeight.toFloat())
                path.close()

                // Set up paint for mask
                val paint = Paint()
                paint.isAntiAlias = true
                paint.style = Paint.Style.FILL
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)

                // Draw mask onto the canvas
                canvas.drawPath(path, paint)

                // Create the puzzle piece using the masked bitmap
                val puzzlePiece = PuzzlePiece(applicationContext)
                puzzlePiece.setImageBitmap(pieceBitmap)
                puzzlePiece.x = xCoord.toFloat()
                puzzlePiece.y = yCoord.toFloat()

                pieces.add(puzzlePiece)

                xCoord += pieceWidth
            }

            yCoord += pieceHeight
        }

        return pieces
    }
}
*/
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.puzzlegame.R


class PuzzleActivity : AppCompatActivity() {

    private lateinit var puzzleGridLayout: GridLayout
    private lateinit var solveButton: Button
    private val puzzlePieces = mutableListOf<PuzzlePiece>()
    private lateinit var originalBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle)

        puzzleGridLayout = findViewById(R.id.puzzleGridLayout)
        solveButton = findViewById(R.id.solveButton)

        originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.img)

        generatePuzzlePieces()

        solveButton.setOnClickListener {
            solvePuzzle()
        }
    }

    private fun generatePuzzlePieces() {
        val shuffledPieces = mutableListOf<Int>()
        for (i in 0 until 9) {
            shuffledPieces.add(i)
        }
        shuffledPieces.shuffle()

        for (i in 0 until 9) {
            val piece = PuzzlePiece(this)
            val row = shuffledPieces[i] / 3
            val col = shuffledPieces[i] % 3
            piece.setImageBitmap(Bitmap.createBitmap(originalBitmap, col * 100, row * 100, 100, 100))
            piece.originalPositionX = col
            piece.originalPositionY = row
            piece.currentPositionX = col
            piece.currentPositionY = row
            piece.layoutParams = GridLayout.LayoutParams().apply {
                width = 100
                height = 100
                columnSpec = GridLayout.spec(col)
                rowSpec = GridLayout.spec(row)
            }
            piece.setOnClickListener {
                movePiece(piece)
            }
            puzzlePieces.add(piece)
            puzzleGridLayout.addView(piece)
        }
    }

    private fun movePiece(piece: PuzzlePiece) {
        val emptyPiece = puzzlePieces.find { it.isCorrectPosition }
        if (isAdjacent(piece, emptyPiece)) {
            val tempX = piece.currentPositionX
            val tempY = piece.currentPositionY
            piece.currentPositionX = emptyPiece!!.currentPositionX
            piece.currentPositionY = emptyPiece.currentPositionY
            emptyPiece.currentPositionX = tempX
            emptyPiece.currentPositionY = tempY
            updateGridLayout()
            checkWin()
        }
    }

    private fun isAdjacent(piece1: PuzzlePiece, piece2: PuzzlePiece?): Boolean {
        return piece2 != null && ((piece1.currentPositionX == piece2.currentPositionX &&
                Math.abs(piece1.currentPositionY - piece2.currentPositionY) == 1) ||
                (piece1.currentPositionY == piece2.currentPositionY &&
                        Math.abs(piece1.currentPositionX - piece2.currentPositionX) == 1))
    }

    private fun updateGridLayout() {
        puzzleGridLayout.removeAllViews()
        for (piece in puzzlePieces) {
            puzzleGridLayout.addView(piece)
        }
    }

    private fun checkWin() {
        var correctPieces = 0
        for (piece in puzzlePieces) {
            if (piece.originalPositionX == piece.currentPositionX &&
                piece.originalPositionY == piece.currentPositionY
            ) {
                correctPieces++
                piece.isCorrectPosition = true
            } else {
                piece.isCorrectPosition = false
            }
        }
        if (correctPieces == 9) {
            solveButton.visibility = View.GONE
            // Display a message or perform any other actions for winning
        }
    }

    private fun solvePuzzle() {
        // Reset puzzle pieces to their original positions
        for (piece in puzzlePieces) {
            piece.currentPositionX = piece.originalPositionX
            piece.currentPositionY = piece.originalPositionY
        }
        updateGridLayout()
        solveButton.visibility = View.VISIBLE
    }
    fun checkGameOver() {
        // Logic to check if the puzzle is complete
        var allPiecesCorrect = true
        for (piece in puzzlePieces) {
            if (!piece.isCorrectPosition) {
                allPiecesCorrect = false
                break
            }
        }
        if (allPiecesCorrect) {
            // Puzzle is complete, show a message or perform any other action
            Toast.makeText(this, "Congratulations! Puzzle complete.", Toast.LENGTH_SHORT).show()
        }
    }
}
