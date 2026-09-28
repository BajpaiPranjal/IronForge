package com.example.workouttracker.data.local

import androidx.room.TypeConverter
import org.json.JSONArray

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value == null) return "[]"
        val array = JSONArray()
        value.forEach { array.put(it) }
        return array.toString()
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<String>()
        val array = JSONArray(value)
        for (i in 0 until array.length()) {
            list.add(array.getString(i))
        }
        return list
    }

    @TypeConverter
    fun fromFloatList(value: List<Float>?): String {
        if (value == null) return "[]"
        val array = JSONArray()
        value.forEach { array.put(it.toDouble()) }
        return array.toString()
    }

    @TypeConverter
    fun toFloatList(value: String?): List<Float> {
        if (value.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<Float>()
        val array = JSONArray(value)
        for (i in 0 until array.length()) {
            list.add(array.getDouble(i).toFloat())
        }
        return list
    }
}
