package com.example.project2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log


private const val TAG = "main"
private const val EXTRA_ALIVE_LIST = "Alive_List"

class MainActivity : AppCompatActivity(),
        GridFragment.Callbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gridFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (gridFragment == null) {
            val fragment = GridFragment()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }

    override fun onCloneButtonClicked(aliveList: ArrayList<Int>?) {
        val fragment = GridFragment.newInstance(aliveList)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}