package com.example.in2000_project.LightningHistory

import android.app.DatePickerDialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import com.example.in2000_project.BaseActivity
import com.example.in2000_project.R

//import com.example.in2000_project.LightningHistoryActivity.His

import java.util.*

class LightningHistoryActivity : BaseActivity() {
//    val tempPlaceSelection = arrayOf("Oslo, Drammen, Lillestrøm", "Trondheim")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lightning_history)
        super.attachBackButton()
        var toolbar : Toolbar = findViewById(R.id.my_toolbar)
        toolbar.title = getString(R.string.lightningHistory)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.content_frame, MapWithoutSearchbar.newInstance(),
                "map_fragment").commit()
        }
        inflateDialog()

        val searchbar = findViewById<SearchView>(R.id.select_area_and_date)
        searchbar.setOnClickListener { inflateDialog() }
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

    private fun inflateDialog(){
        val dialogAreaSelect = AlertDialog.Builder(this@LightningHistoryActivity)
        val view = layoutInflater.inflate(R.layout.dialog_select_area_and_date, null)
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
    }
}
