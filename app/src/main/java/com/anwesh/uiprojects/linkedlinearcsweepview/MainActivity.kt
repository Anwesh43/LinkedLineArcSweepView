package com.anwesh.uiprojects.linkedlinearcsweepview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.linearcsweepview.LineArcSweepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LineArcSweepView.create(this)
    }
}
