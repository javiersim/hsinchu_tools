package com.example.mymap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_advance.*

class AdvanceActivity : AppCompatActivity() {

    companion object {
        var itemlist = mutableListOf<String>()
        var mylat = 9999.0
        var mylng = 9999.0
        var userlat = 9999.0
        var userlng = 9999.0
        var advance = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advance)

        itemlist = mutableListOf()
        itemlist.add("0%no%Try search something")

        supportFragmentManager.beginTransaction().add(R.id.fragment_container, FirstFragment(), "frag1").commit()
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, SecondFragment(), "frag2").commit()

        frag1.setOnCheckedChangeListener { _, isCheck ->
            if (isCheck) {
                supportFragmentManager.beginTransaction().hide(supportFragmentManager.findFragmentByTag("frag2")!!).commit()
                supportFragmentManager.beginTransaction().show(supportFragmentManager.findFragmentByTag("frag1")!!).commit()
            }
        }

        frag2.setOnCheckedChangeListener { _, isCheck ->
            if (isCheck) {
                supportFragmentManager.beginTransaction().hide(supportFragmentManager.findFragmentByTag("frag1")!!).commit()
                supportFragmentManager.beginTransaction().show(supportFragmentManager.findFragmentByTag("frag2")!!).commit()
            }
        }

        mylat = intent.getDoubleExtra("mylat", 9999.0)
        mylng = intent.getDoubleExtra("mylng", 9999.0)

        userlat = intent.getDoubleExtra("userlat", 9999.0)
        userlng = intent.getDoubleExtra("userlng", 9999.0)

        advance = intent.getBooleanExtra("advance", false)
    }

    override fun onStart() {
        super.onStart()
        supportFragmentManager.beginTransaction().hide(supportFragmentManager.findFragmentByTag("frag2")!!).commit()
    }
}
