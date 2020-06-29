package com.example.mymap

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class MyAdapter(context: Context, layoutResource: Int, queries: MutableList<String>): ArrayAdapter<String>(context, layoutResource, queries) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = super.getView(position, convertView, parent)

        val str = getItem(position)?.split("%".toRegex())
        when (str?.get(1)) {
            "no" -> (row as TextView).text = str[2]
            "wifi" -> (row as TextView).text = if (str.size==7) {
                                                    "wifi@" + str?.get(2) + ": " + str?.get(3) + "   (" + String.format("%.3f", str?.get(6).toDoubleOrNull()) + "km)"
                                                } else {
                                                    "wifi@" + str?.get(2) + ": " + str?.get(3)
                                                }
            "conv", "custom" -> (row as TextView).text = if (str.size==7) {
                                                            str?.get(2) + ": " + str?.get(3) + "   (" + String.format("%.3f", str?.get(6).toDoubleOrNull()) + "km)"
                                                        } else {
                                                            str?.get(2) + ": " + str?.get(3)
                                                        }
            else -> (row as TextView).text = "wrong format"
        }
        return row
    }
}