package com.example.brickbreaker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.proyecto.proyect_games.R
import com.proyecto.proyect_games.brickbreaker.BrickBreacker_Activity

class homepages_brickbreacker : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepages_brickbreacker)

        var button2=findViewById<Button>(R.id.button2)
        button2.setOnClickListener {
            val intent = Intent(this, BrickBreacker_Activity::class.java)
            startActivity(intent)
        }
    }
}