package com.example.mysocialapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.mysocialapp.daos.PostDao
import kotlinx.android.synthetic.main.activity_create_post.*
import kotlinx.coroutines.delay

class CreatePostActivity : AppCompatActivity() {

    private lateinit var postDao: PostDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        postDao = PostDao()

        postButton.setOnClickListener {
            //Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show()
            val input = postInput.text.toString().trim()

            if(input.isNotEmpty()){
                //Toast.makeText(this, "post submitted $input", Toast.LENGTH_SHORT).show()
                postDao.addPost(input)
                finish()
            }else{
                Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show()
            }
        }
    }
}