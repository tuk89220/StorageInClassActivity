package com.example.networkapp

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.io.*
import android.net.Uri
import android.provider.Settings

private const val AUTO_SAVE_KEY = "auto_save"
class MainActivity : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    lateinit var titleTextView: TextView
    lateinit var descriptionTextView: TextView
    lateinit var numberEditText: EditText
    lateinit var showButton: Button
    lateinit var comicImageView: ImageView

    private val internalFilename = "my_file"
    private lateinit var file: File


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // ATTENTION: This was auto-generated to handle app links.
        //val appLinkIntent: Intent = intent
        //val appLinkAction: String? = appLinkIntent.action
        //val appLinkData: Uri? = appLinkIntent.data

        findViewById<Button>(R.id.registerButton).setOnClickListener() {
            try {
                val intent = Intent(
                    Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                    Uri.parse("package:${packageName}")
                )
                startActivity(intent)
          } catch (e: java.lang.Exception) {
                e.printStackTrace()
           }
        }
        file = File(filesDir, internalFilename)

        requestQueue = Volley.newRequestQueue(this)


        titleTextView = findViewById<TextView>(R.id.comicTitleTextView)
        descriptionTextView = findViewById<TextView>(R.id.comicDescriptionTextView)
        numberEditText = findViewById<EditText>(R.id.comicNumberEditText)
        showButton = findViewById<Button>(R.id.showComicButton)
        comicImageView = findViewById<ImageView>(R.id.comicImageView)

        if (file.exists()) {
            try {
                val br = BufferedReader(FileReader(file))
                val text = StringBuilder()
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    text.append(line)
                    text.append('\n')
                }
                br.close()
                showComic(JSONObject(text.toString()))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        showButton.setOnClickListener {
            downloadComic(numberEditText.text.toString())
        }

        if(intent.action == Intent.ACTION_VIEW){
            intent.data?.path?.run{
                downloadComic(replace("/", ""))
            }
        }

    }



    override fun onStop() {
        super.onStop()
        // Save whenever activity goes to the background
            try {
                val outputStream = FileOutputStream(file)
                outputStream.write(numberEditText.text.toString().toByteArray())
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Delete file if auto save is turned off
        // onDestroy() does not fire onBackPressed in later APIs
    }

    private fun downloadComic (comicId: String) {
        val url = "https://xkcd.com/$comicId/info.0.json"
        requestQueue.add (
            JsonObjectRequest(url, {
                showComic(it)
                saveComic(it)                       }, {
            })
        )
    }

    private fun showComic (comicObject: JSONObject) {
        titleTextView.text = comicObject.getString("title")
        descriptionTextView.text = comicObject.getString("alt")
        Picasso.get().load(comicObject.getString("img")).into(comicImageView)
    }

    private fun saveComic(JsonObject: JSONObject){
        val outputStream = FileOutputStream(file)
        outputStream.write(JsonObject.toString().toByteArray())
        outputStream.close()
    }

}