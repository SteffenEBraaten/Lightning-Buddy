package com.example.in2000_project.intro


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_slider.*

import com.example.in2000_project.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class SliderFragment : Fragment() {

    private var tempTitle : String = ""
    private var tempContent : String = ""
    private var tempPath : Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_slider, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        slider_title.text = tempTitle
        slider_content.text = tempContent
        slider_image.setImageResource(tempPath)
    }

    //Sets the title of the sliderFragment
    fun setTitle(title : String){
        tempTitle = title
    }

    // Sets the content for the sliderFragment
    fun setContent(content : String){
        tempContent = content
    }

    fun setPath(path : Int){
        tempPath = path
    }
}
