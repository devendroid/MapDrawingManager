package com.devs.mapdrawingmanager

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.maps.GoogleMap

/**
 * Created by ${Deven} on 31/7/19.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setTitle(getString(R.string.app_name))
        supportFragmentManager.beginTransaction().add(R.id.fragment_container,
            FragMap(), "FragMap").commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            //android.R.id.home ->
            //    this.finish()
            R.id.action_remove ->
                // Transfer click in fragment
                return super.onOptionsItemSelected(item)
        }
        return true
    }
}

fun Context.log(text: CharSequence) {
    Log.i("MainActivity","== "+text)
}
