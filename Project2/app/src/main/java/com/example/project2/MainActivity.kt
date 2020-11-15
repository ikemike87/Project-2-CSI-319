package com.example.project2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log


private const val TAG = "main"
private const val EXTRA_ALIVE_LIST = "Alive_List"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gridFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        val aliveList = intent?.getIntArrayExtra(EXTRA_ALIVE_LIST)

        if (gridFragment == null) {
            val fragment = GridFragment.newInstance(aliveList)
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }
}