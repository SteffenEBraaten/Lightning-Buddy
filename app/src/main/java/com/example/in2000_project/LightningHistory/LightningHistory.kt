package com.example.in2000_project.LightningHistory

import android.app.DatePickerDialog
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.in2000_project.BaseActivity
import com.example.in2000_project.R
//import com.example.in2000_project.LightningHistory.His

import kotlinx.android.synthetic.main.activity_lightning_history.*
import kotlinx.android.synthetic.main.activity_lightning_history.view.*
import kotlinx.android.synthetic.main.dialog_select_area.*
import java.util.*
import java.util.logging.Logger

class LightningHistory : BaseActivity() {
//    val tempPlaceSelection = arrayOf("Oslo, Drammen, Lillestrøm", "Trondheim")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lightning_history)
        super.attachBackButton()
        var toolbar : Toolbar = findViewById(R.id.my_toolbar)
        toolbar.title = getString(R.string.lightningHistory)

        val dialogAreaSelect = AlertDialog.Builder(this@LightningHistory)
        val view = layoutInflater.inflate(R.layout.dialog_select_area, null)
        dialogAreaSelect.setView(view)

        val areaSelectLabel = view.findViewById<TextView>(R.id.areaSelectLabel)
        val fromDateLabel = view.findViewById<TextView>(R.id.fromDateLabel)
        val toDateLabel = view.findViewById<TextView>(R.id.toDateLabel)
        areaSelectLabel.setText("Selec an area")
        fromDateLabel.setText("From")
        toDateLabel.setText("To")

        val fromDateEditText = view.findViewById<EditText>(R.id.fromDate)
        val toDateEditText = view.findViewById<EditText>(R.id.toDate)
        fromDateEditText.setOnClickListener { v -> clickDatePicker(v, fromDateEditText) }
        toDateEditText.setOnClickListener { v -> clickDatePicker(v, toDateEditText)}

        dialogAreaSelect.setPositiveButton("Search") { dialog, whichButton -> var placeHolder = 123}
        var dialog = dialogAreaSelect.create()

        dialog.setOnShowListener{
            val searchButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            searchButton.setOnClickListener{
                var validDates  = true
                val fromDateString = fromDateEditText.text.toString()
                val toDateString = toDateEditText.text.toString()
                if ( fromDateString == "" || toDateString == ""){
                    validDates = false
                    if (fromDateString == ""){
                        Toast.makeText(this, "Please select a date from", Toast.LENGTH_LONG).show()
                        fromDateEditText.performClick()
                    }
                    else{
                        Toast.makeText(this, "Please select a date to", Toast.LENGTH_LONG).show()
                        toDateEditText.performClick()
                    }
                }

                if (validDates){
                    dialog.dismiss()
                }
            }
        }
        dialog.show()





//        supportFragmentManager.beginTransaction().add(R.id.content_frame, MapFragment.newInstance(),
//            "Map Fragment").commit()
    }

    private fun clickDatePicker(view: View?, editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            // Display Selected date in Toast
            Toast.makeText(this, """$dayOfMonth - ${monthOfYear + 1} - $year""", Toast.LENGTH_LONG).show()
            editText.setText("""$dayOfMonth - ${monthOfYear + 1} - $year""")
        }, year, month, day)
        dpd.show()
    }
}
