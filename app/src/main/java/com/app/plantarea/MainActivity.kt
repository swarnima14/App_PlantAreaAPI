package com.app.plantarea

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import pyxis.uzuki.live.mediaresizer.MediaResizer
import pyxis.uzuki.live.mediaresizer.data.ImageResizeOption
import pyxis.uzuki.live.mediaresizer.data.ResizeOption
import pyxis.uzuki.live.mediaresizer.model.ImageMode
import pyxis.uzuki.live.mediaresizer.model.ScanRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class MainActivity : AppCompatActivity(), UploadRequestBody.UploadCallback {

    lateinit var bitmap: Bitmap
    lateinit var date: String
    var photoFile: File? = null
    lateinit var fileProvider: Uri
    var uri: Uri? = null
    val FILE_NAME = "pic"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSelect.setOnClickListener {
            tvResult.text = "Area: "
            askForPermission()
        }

        btnSend.setOnClickListener {
            if(photoFile != null)
            sendImage()
            else
                Toast.makeText(this, "Select image first.", Toast.LENGTH_SHORT).show()
        }

    }

    private fun sendImage() {

        progressBar.progress = 0
        progressBar.visibility = View.VISIBLE
        val body = UploadRequestBody(photoFile!!, "multipart/form-data", this)

        MyAPI().uploadImage(
            MultipartBody.Part.createFormData("image", photoFile!!.name, body)
        ).enqueue(object: Callback<Number>{
            override fun onFailure(call: Call<Number>, t: Throwable) {
                progressBar.progress = 0
                progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Error: "+t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<Number>,
                response: Response<Number>
            ) {
                progressBar.progress = 100
                progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Uploaded", Toast.LENGTH_SHORT).show()
                tvResult.text = "Area: ${response.body()}"
            }

        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 100 && resultCode == Activity.RESULT_OK){
            ivImage.setImageURI(data?.data)
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, data?.data)
        }
        if(requestCode == 99 && resultCode == Activity.RESULT_OK) {

            bitmap = BitmapFactory.decodeFile(photoFile!!.path)
            ivImage.setImageBitmap(bitmap)

            val resizeOption = ImageResizeOption.Builder()
                .setImageProcessMode(ImageMode.ResizeAndCompress)
                .setImageResolution(1280, 720)
                .setBitmapFilter(false)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .setCompressQuality(75)
                .setScanRequest(ScanRequest.TRUE)
                .build()

            val option = ResizeOption.Builder()
                .setMediaType(pyxis.uzuki.live.mediaresizer.model.MediaType.IMAGE)
                .setImageResizeOption(resizeOption)
                .setTargetPath(photoFile!!.absolutePath)
                .setOutputPath(photoFile!!.absolutePath)
                .build()

            MediaResizer.process(option)

            uri = Uri.fromFile(photoFile)

        }
    }

    fun askForPermission()
    {
        if((ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) !=
            PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 11)
        }
        else
        {
            openCamera()
        }
    }

    fun openCamera()
    {
        var camIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        photoFile = getPhotoFile(FILE_NAME)

        fileProvider = FileProvider.getUriForFile(this,"com.app.plantarea.fileprovider", photoFile!!)
        camIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

        startActivityForResult(camIntent, 99)
    }

    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName,".jpg", storageDirectory)
    }

    override fun onProgressUpdate(percentage: Int) {
        progressBar.progress = percentage
    }

    override fun onBackPressed() {
        super.onBackPressed()

        progressBar.visibility = View.GONE
        ivImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.no_image))
        tvResult.text = "Select image first."
    }
}