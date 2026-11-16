package com.example.parcialkotlinapp

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MisionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mision)

        findViewById<ImageButton>(R.id.btnVolverMision).setOnClickListener {
            finish() 
        }
    }
}
