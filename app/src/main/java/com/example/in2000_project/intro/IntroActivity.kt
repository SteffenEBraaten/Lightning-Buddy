package com.example.in2000_project.intro

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import com.example.in2000_project.R
import com.example.in2000_project.maps.MainActivity
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : AppCompatActivity() {

    private val slider1 = SliderFragment()
    private val slider2 = SliderFragment()
    private val slider3 = SliderFragment()

    lateinit var prefrence : SharedPreferences
    private val intro_flag = "Intro"

    lateinit var adapter: MyPageAdapter
    lateinit var activity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        prefrence = getSharedPreferences("FirstTimeIntro", Context.MODE_PRIVATE)
        activity = this

        if(!prefrence.getBoolean(intro_flag, true)){
            startActivity(Intent(activity, MainActivity::class.java))
            finish()
        }

        slider1.setPath(R.drawable.lightning_symbol)
        slider1.setTitle(getString(R.string.slider1_Title))
        slider1.setContent(getString(R.string.slider1_description))


        slider2.setPath(R.drawable.lightning_symbol)
        slider2.setTitle(getString(R.string.slider2_Title))
        slider2.setContent(getString(R.string.slider2_description))


        slider3.setPath(R.drawable.lightning_symbol)
        slider3.setTitle(getString(R.string.slider3_Title))
        slider3.setContent(getString(R.string.slider3_description))


        adapter = MyPageAdapter(supportFragmentManager)

        adapter.sliderList.add(slider1)
        adapter.sliderList.add(slider2)
        adapter.sliderList.add(slider3)

        intro_viewPager.adapter = adapter

        // Initializing btn_next
        intro_btn_next.setOnClickListener {
            intro_viewPager.currentItem++
        }

        // Initializing btn_skip
        intro_btn_skip.setOnClickListener {
            goToMainActivity()
        }
        intro_viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(p0: Int) {
                // We're at the last page
                if (p0 == adapter.sliderList.size - 1) {
                    intro_btn_next.text = getText(R.string.done)
                    intro_btn_next.setOnClickListener{
                        goToMainActivity()
                    }
                }
                else {
                    // There is a next
                    //intro_btn_next.text = getText(R.string.next)
                    intro_btn_next.setOnClickListener {
                        intro_viewPager.currentItem++
                    }
                }
                // Change the progressbar color
                when(intro_viewPager.currentItem) {
                    0 ->{
                        intro_progress_1.setTextColor(Color.BLACK)
                        intro_progress_2.setTextColor(Color.GRAY)
                        intro_progress_3.setTextColor(Color.GRAY)
                    }
                    1 -> {
                        intro_progress_1.setTextColor(Color.GRAY)
                        intro_progress_2.setTextColor(Color.BLACK)
                        intro_progress_3.setTextColor(Color.GRAY)
                    }
                    2 -> {
                        intro_progress_1.setTextColor(Color.GRAY)
                        intro_progress_2.setTextColor(Color.GRAY)
                        intro_progress_3.setTextColor(Color.BLACK)
                    }
                }
            }
        })
    }

    // Takes user to MainActivity and sets flag to false
    private fun goToMainActivity(){
        startActivity(Intent(activity, MainActivity::class.java))
        finish()
        val editor = prefrence.edit()
        editor.putBoolean(intro_flag, false)
        editor.apply()
    }

    class MyPageAdapter(manager : FragmentManager) : FragmentPagerAdapter(manager) {

        val sliderList : MutableList<Fragment> = ArrayList()

        override fun getItem(p0: Int): Fragment {
            return sliderList[p0]
        }

        override fun getCount(): Int {
            return sliderList.size
        }

    }
}