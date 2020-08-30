package com.integrals.inlens.Activities.kotlin

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.integrals.inlens.Helper.AppConstants
import com.integrals.inlens.Models.PostModel
import com.integrals.inlens.R
import kotlinx.android.synthetic.main.activity_photo_view.*
import java.util.ArrayList

class PhotoViewActivity : AppCompatActivity() {

    internal var appTheme: String? = ""
    var blogArrayList: ArrayList<PostModel> = ArrayList()
    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        val appDataPref = getSharedPreferences(AppConstants.appDataPref, Context.MODE_PRIVATE)
        val appDataPrefEditor = appDataPref.edit()
        if (appDataPref.contains(AppConstants.appDataPref_theme)) {
            appTheme = appDataPref.getString(AppConstants.appDataPref_theme, AppConstants.themeLight)
            if (appTheme == AppConstants.themeLight) {
                setTheme(R.style.AppTheme)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                setTheme(R.style.DarkTheme)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        } else {
            appTheme = AppConstants.themeLight
            appDataPrefEditor.putString(AppConstants.appDataPref_theme, AppConstants.themeLight)
            appDataPrefEditor.commit()
            setTheme(R.style.AppTheme)

        }
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_photo_view)

        blogArrayList = intent.extras!!.getParcelableArrayList("data")
        position = intent.extras!!.getInt("position")


        val adapter = ViewPagerAdapter(blogArrayList,this)
        photoview_viewpager.adapter = adapter
        photoview_viewpager.currentItem = position


    }
}

