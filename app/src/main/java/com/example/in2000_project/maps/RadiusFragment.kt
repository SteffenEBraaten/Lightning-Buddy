package com.example.in2000_project.maps

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SeekBar
import com.example.in2000_project.R

class RadiusFragment: Fragment() {
    private lateinit var fragment: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragment = inflater.inflate(R.layout.set_radius_fragment, container, false)
        setEditTextValue()
        return fragment
    }
    private fun setEditTextValue() {
        val editText: EditText = fragment.findViewById(R.id.radius_input_field)
        val seekbar: SeekBar = fragment.findViewById(R.id.radius_seek)
        seekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                editText.setText(progress.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
    }
}