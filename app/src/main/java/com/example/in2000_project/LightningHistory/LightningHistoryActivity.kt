package com.example.in2000_project.LightningHistory

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import com.example.in2000_project.BaseActivity
import com.example.in2000_project.R
import com.example.in2000_project.maps.MapRepository
import com.example.in2000_project.utils.UalfUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

import java.text.SimpleDateFormat
import java.util.*

class LightningHistoryActivity : BaseActivity() {

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
//        HistoryViewmodel().inflateDialog(this)

        inflateDialog(this)

        val searchbar = findViewById<SearchView>(R.id.select_area_and_date)
//        searchbar.setOnClickListener { HistoryViewmodel().inflateDialog(this) }
        searchbar.setOnClickListener { inflateDialog(this) }
    }

    fun inflateDialog(context: Context){
        val layoutInflater = LayoutInflater.from(context)
        val dialogAreaSelect = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.dialog_select_area_and_date, null)
        dialogAreaSelect.setView(view)

        val areaSelectLabel = view.findViewById<TextView>(R.id.areaSelectLabel)
        val fromDateLabel = view.findViewById<TextView>(R.id.fromDateLabel)
        val toDateLabel = view.findViewById<TextView>(R.id.toDateLabel)
        areaSelectLabel.text = "Selec an area"
        fromDateLabel.text = "From"
        toDateLabel.text = "To"

        val frostApiIsShit = false
        val formater = SimpleDateFormat("yyyy-MM-dd")
        val calendar = Calendar.getInstance()
        val to = calendar.time
        calendar.add(Calendar.DATE, -1)
        val from = calendar.time

        val fromDateEditText = view.findViewById<EditText>(R.id.fromDate)
        val toDateEditText = view.findViewById<EditText>(R.id.toDate)
        fromDateEditText.setText(formater.format(from))
        toDateEditText.setText(formater.format(to))

        fromDateEditText.setOnClickListener { v -> clickDatePicker(v, fromDateEditText, context, frostApiIsShit) }
        toDateEditText.setOnClickListener { v -> clickDatePicker(v, toDateEditText, context, frostApiIsShit)}

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
                        Toast.makeText(context, "Please select a date from", Toast.LENGTH_LONG).show()
                        fromDateEditText.performClick()
                    }
                    else{
                        Toast.makeText(context, "Please select a date to", Toast.LENGTH_LONG).show()
                        toDateEditText.performClick()
                    }
                }
                val selectedFrom = formater.parse(fromDateString)
                val selectedTo = formater.parse(toDateString)
                if (selectedFrom > selectedTo) {
                    validDates = false
                    Toast.makeText(context, "Invalid time period!", Toast.LENGTH_LONG).show()

                }
                if (validDates){
                    HistoryViewmodel().handleSearh(context, selectedFrom, selectedTo)
                    dialog.dismiss()
                }

            }
        }
        dialog.show()
    }



    private fun clickDatePicker(view: View?, editText: EditText, context: Context, frostApiIsShit: Boolean) {
        if (frostApiIsShit){
            Toast.makeText(context, "FrostAPI currently only support history from yesterday, date selection is disabled", Toast.LENGTH_LONG).show()
            return
        }

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            // Display Selected date in Toast
            var textMonth = (monthOfYear + 1).toString()
            var textDate = dayOfMonth.toString()
            if (monthOfYear + 1 < 10){
                textMonth = "0$textMonth"
            }
            if (dayOfMonth < 10){
                textDate = "0$textDate"
            }
//            Toast.makeText(context, "$year-${textMonth}-$textDate", Toast.LENGTH_LONG).show()
            editText.setText("$year-${textMonth}-$textDate")
        }, year, month, day)
        dpd.show()
    }



}
