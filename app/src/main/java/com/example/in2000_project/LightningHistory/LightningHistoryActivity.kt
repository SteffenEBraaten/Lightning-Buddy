package com.example.in2000_project.LightningHistory

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.example.in2000_project.BaseActivity
import com.example.in2000_project.R
import com.example.in2000_project.maps.MapFragment
import com.example.in2000_project.maps.MapFragment.SavedMarkers
import com.example.in2000_project.maps.MapRepository
import com.example.in2000_project.utils.UalfUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_map_without_searchbar.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.Exception

import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class LightningHistoryActivity : BaseActivity(), AdapterView.OnItemSelectedListener{

    var markers = HashMap<Int, SavedMarkers>()
    val spinnerList: ArrayList<String> = ArrayList()
//    lateinit var spinner: Spinner
    var selectedMarker: SavedMarkers = SavedMarkers("All", 62.116681, 16.121960, 800000.0)
    var selectedMarkerIndex = 0
    var currentToast: Toast? = null

    lateinit var to: Date
    lateinit var from: Date
    lateinit var today: Date
    var mapFrag: MapWithoutSearchbar? = null
    var calendar: Calendar = Calendar.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val defaulAreaChoices = resources.getStringArray(R.array.defualtAreaChoices)
        Log.e("Spinner length", spinnerList.size.toString())
        for (i in defaulAreaChoices.indices){
            val jsonObj = JSONObject(defaulAreaChoices[i])
            val savedMarker = SavedMarkers( jsonObj.getString("name"),
                                            jsonObj.getDouble("lat"),
                                            jsonObj.getDouble("long"),
                                            jsonObj.getDouble("radius"))
            markers.put(i, savedMarker)
//            spinnerList[i] = savedMarker.name
            spinnerList.add(savedMarker.name)
        }

        Log.e("Default Markers", markers.toString())

        val jsonLinkedList = getPrefs()!!.getString("SavedMarkers", "")
//        val jsonLinkedList = getPrefs()!!.getString("SavedMarkers", null)
        if (jsonLinkedList != "") {
            val savedMarkersList: ArrayList<SavedMarkers> = Gson().fromJson(jsonLinkedList, object: TypeToken<MutableList<SavedMarkers>>(){}.type)
//            Log.e("SavedMarkerList", savedMarkersList.toString())
            val offset = spinnerList.size
            for (i in savedMarkersList.indices){
                markers.put(i + offset, savedMarkersList[i])
                spinnerList.add(savedMarkersList[i].name)
            }
            Log.e("HistoryAct", savedMarkersList.toString())
        }
        else{
            Log.e("HistoryAct", "No Saved markers")
        }



        setContentView(R.layout.activity_lightning_history)
        super.attachBackButton()
        var toolbar : Toolbar = findViewById(R.id.my_toolbar)
        toolbar.title = getString(R.string.lightningHistory)

        supportFragmentManager.beginTransaction().add(R.id.content_frame, MapWithoutSearchbar.newInstance(),
            "mapWithoutSearchbar").commit()


        val searchbar = findViewById<SearchView>(R.id.select_area_and_date)
        searchbar.setOnClickListener { inflateDialog(this, this.from, this.to) }
    }

    override fun onResume() {
        this.today = Calendar.getInstance().time
//        val jsonLinkedList = getPrefs()!!.getString("SavedMarkers", null)
//        if (jsonLinkedList != null) {
//            val savedMarkersList: Array<SavedMarkers> = Gson().fromJson(jsonLinkedList, object: TypeToken<MutableSet<MapFragment.SavedMarkers>>(){}.type)
//            val offset = spinnerList.size
//            for (i in savedMarkersList.indices){
//                markers.put(i + offset, savedMarkersList[i])
//                spinnerList.add(savedMarkersList[i].name)
//            }
//            Log.e("HistoryAct", savedMarkersList.toString())
//        }
//        else{
//            Log.e("HistoryAct", "No Saved markers")
//        }



        super.onResume()
    }

    override fun onStart() {

        this.to = calendar.time
        this.calendar.add(Calendar.DATE, -1)
        this.from = calendar.time
        this.mapFrag = supportFragmentManager.findFragmentById(R.id.map_frag) as MapWithoutSearchbar?
        Log.e("MAP FRAG FROM Lightning", "${this.mapFrag}")

        inflateDialog(this, this.from, this.to)
        super.onStart()
    }


    override fun onNothingSelected(parent: AdapterView<*>?) {}
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        this.selectedMarker = markers.getOrDefault(position, SavedMarkers("All", 62.116681, 16.121960, 800000.0))
        this.selectedMarkerIndex = position
    }

    fun inflateDialog(context: Context, from: Date, to: Date){
        val layoutInflater = LayoutInflater.from(context)
        val dialogAreaSelect = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.dialog_select_area_and_date, null)
        dialogAreaSelect.setView(view)

        val spinner: Spinner = view.findViewById<Spinner>(R.id.areaSelect)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerList)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
        spinner.onItemSelectedListener = this
        spinner.setSelection(this.selectedMarkerIndex)


        val areaSelectLabel = view.findViewById<TextView>(R.id.areaSelectLabel)
        val fromDateLabel = view.findViewById<TextView>(R.id.fromDateLabel)
        val toDateLabel = view.findViewById<TextView>(R.id.toDateLabel)
        areaSelectLabel.text = "Selec an area"
        fromDateLabel.text = "From"
        toDateLabel.text = "To"



        val fromDateEditText = view.findViewById<EditText>(R.id.fromDate)
        val toDateEditText = view.findViewById<EditText>(R.id.toDate)
        fromDateEditText.setText(SimpleDateFormat("yyyy-MM-dd").format(from))
        toDateEditText.setText(SimpleDateFormat("yyyy-MM-dd").format(to))

        fromDateEditText.setOnClickListener { v -> clickDatePicker(v, fromDateEditText, context, from) }
        toDateEditText.setOnClickListener { v -> clickDatePicker(v, toDateEditText, context, to)}

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
                        dispayToast(context, "Please select a date from", Toast.LENGTH_LONG)
                        fromDateEditText.performClick()
                    }
                    else{
                        dispayToast(context, "Please select a date to", Toast.LENGTH_LONG)
                        toDateEditText.performClick()
                    }
                }
                val selectedFrom = SimpleDateFormat("yyyy-MM-dd").parse(fromDateString)
                val selectedTo = SimpleDateFormat("yyyy-MM-dd").parse(toDateString)
                if (selectedFrom > selectedTo) {
                    validDates = false
                    dispayToast(context, "Invalid time period!", Toast.LENGTH_LONG)

                }

                if (validDates){
                    if (selectedFrom == selectedTo) {
                        selectedFrom.time -= 1000 * 60 * 60 * 24
                    }
                    HistoryViewmodel().handleSearh(context, selectedFrom, selectedTo, this, this.mapFrag)
                    dispayToast(context, "Loading data...", Toast.LENGTH_SHORT)
                    dialog.dismiss()
                }

            }
        }
        dialog.show()
    }

    fun dispayToast(context: Context, text: String, length: Int) {

        this.currentToast?.cancel()
        this.currentToast = Toast.makeText(context, text, length)
        this.currentToast?.show()
    }

    private fun clickDatePicker(view: View?, editText: EditText, context: Context, initDate: Date) {
        val localCalendar = Calendar.getInstance()
        localCalendar.time = initDate
        val year = localCalendar.get(Calendar.YEAR)
        val month = localCalendar.get(Calendar.MONTH)
        val day = localCalendar.get(Calendar.DAY_OF_MONTH)

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
            if (this.from == initDate){
                this.from = SimpleDateFormat("yyy-MM-dd").parse("$year-${textMonth}-$textDate")
            }
            else{
                this.to = SimpleDateFormat("yyy-MM-dd").parse("$year-${textMonth}-$textDate")
            }
        }, year, month, day)

        dpd.datePicker.maxDate = this.today.time
        dpd.show()
    }



}
