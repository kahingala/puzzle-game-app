package com.example.puzzlegame

import PuzzleActivity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var mCurrentPhotoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val am = assets
        try {
            val files = am.list("img")
            val grid = findViewById<GridView>(R.id.grid)

            grid.adapter = ImageAdapter(this@MainActivity)
            grid.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
                val intent = Intent(applicationContext, PuzzleActivity::class.java)
                intent.putExtra("assetName", files!![i % files.size])
                startActivity(intent)
            }
        } catch (e: IOException) {
            Toast.makeText(this@MainActivity, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    fun onImageCameraClicked(view: View) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
            photoFile?.let {
                val photoUri = FileProvider.getUriForFile(
                    this@MainActivity,
                    applicationContext.packageName + ".fileprovider",
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    fun onImageGalleryClicked(view: View) {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_READ_EXTERNAL_STORAGE
            )
        } else {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_$timestamp"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            imageFileName, ".jpg", storageDir
        ).apply {
            mCurrentPhotoPath = absolutePath
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onImageGalleryClicked(View(this@MainActivity))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val intent = Intent(this@MainActivity, PuzzleActivity::class.java)
            intent.putExtra("mCurrentPhotoPath", mCurrentPhotoPath)
            startActivity(intent)
        } else if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
            val uri = data?.data
            uri?.let {
                val intent = Intent(this@MainActivity, PuzzleActivity::class.java)
                intent.putExtra("mCurrentPhotoUri", uri.toString())
                startActivity(intent)
            }
        }
    }

    companion object {
        private const val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 2
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 3
        private const val REQUEST_IMAGE_GALLERY = 4
    }
}
