package com.b9default.a8_app_utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

object SharePrefUtils {
    private var mSharePref: SharedPreferences? = null

    private const val PREF_NAME = "data"

    fun init(context: Context?) {
        if (mSharePref == null) {
            mSharePref = PreferenceManager.getDefaultSharedPreferences(context)
        }
    }


    fun isRated(context: Context): Boolean {
        val pre = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        return pre.getBoolean("rated", false)
    }


    fun forceRated(context: Context) {
        val pre = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = pre.edit()
        editor.putBoolean("rated", true)
        editor.commit()
    }

    fun countChat(context: Context): Int {
        val pre = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        return pre.getInt("countChat", 0)
    }


    fun increaseCountChat(context: Context){
        val pre = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = pre.edit()
        editor.putInt("countChat", countChat(context) + 1)
        editor.commit()
    }


    /*lưu các biến kiểu Int*/
    fun setInt(context: Context,nameString: String, limitNumber: Int) {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = pre.edit()
        editor.putInt(nameString, limitNumber)
        editor.apply()
    }
    fun getInt(context: Context, nameString: String): Int {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pre.getInt(nameString, 0)
    }

    /*lưu các biến kiểu String*/
    fun setString(context: Context,nameString: String, url: String) {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = pre.edit()
        editor.putString(nameString, url)
        editor.apply()
    }
    fun getString(context: Context, nameString: String): String? {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pre.getString(nameString, null)
    }

    /*lưu các biến kiểu Long*/
    fun setLong(context: Context,nameString: String, long: Long) {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = pre.edit()
        editor.putLong(nameString, long)
        editor.apply()
    }
    fun getLong(context: Context, nameString: String): Long {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pre.getLong(nameString, 0L)
    }


    /*lưu các biến kiểu Boolean*/
    fun setBoolean(context: Context,nameString: String, boolean: Boolean) {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = pre.edit()
        editor.putBoolean(nameString, boolean)
        editor.apply()
    }
    fun getBoolean(context: Context, nameString: String): Boolean {
        val pre = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pre.getBoolean(nameString, false)
    }
}