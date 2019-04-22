package com.example.in2000_project.LightningHistory

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import com.example.in2000_project.BaseActivity
import com.example.in2000_project.R

import kotlinx.android.synthetic.main.activity_lightning_history.*

class LightningHistory : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lightning_history)
        super.attachBackButton()
    }

}
