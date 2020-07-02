package com.example.mymap

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.Toast
import com.example.mymap.MapsActivity.Companion.dbhelper
import com.example.mymap.AdvanceActivity.Companion.itemlist
import com.example.mymap.AdvanceActivity.Companion.mylat
import com.example.mymap.AdvanceActivity.Companion.mylng
import com.example.mymap.AdvanceActivity.Companion.userlat
import com.example.mymap.AdvanceActivity.Companion.userlng
import kotlinx.android.synthetic.main.fragment_first.*
import kotlin.math.pow

class FirstFragment : Fragment() {

    private fun Boolean.int(n: Int = 0): Int{
        return if (this) 2f.pow(n).toInt() else 0
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        frag1_conv.setOnCheckedChangeListener { _, isChecked ->
            frag1_conv_status1.isClickable = isChecked
            frag1_conv_status2.isClickable = isChecked
            frag1_conv_status3.isClickable = isChecked
            frag1_conv_type1.isClickable = isChecked
            frag1_conv_type2.isClickable = isChecked
            frag1_conv_type3.isClickable = isChecked
            frag1_conv_type4.isClickable = isChecked

            val color = if (isChecked) {
                R.color.enabled
            } else {
                R.color.disabled
            }

            frag1_conv_status1.buttonTintList = resources.getColorStateList(color, null)
            frag1_conv_status2.buttonTintList = resources.getColorStateList(color, null)
            frag1_conv_status3.buttonTintList = resources.getColorStateList(color, null)
            frag1_conv_type1.buttonTintList = resources.getColorStateList(color, null)
            frag1_conv_type2.buttonTintList = resources.getColorStateList(color, null)
            frag1_conv_type3.buttonTintList = resources.getColorStateList(color, null)
            frag1_conv_type4.buttonTintList = resources.getColorStateList(color, null)
        }

        frag1_area.setOnCheckedChangeListener { _, isChecked ->
            frag1_area_current.isClickable = isChecked
            frag1_area_user.isClickable = isChecked
            frag1_area_radius.isEnabled = isChecked

            val color = if (isChecked) {
                R.color.enabled
            } else {
                R.color.disabled
            }

            frag1_area_current.buttonTintList = resources.getColorStateList(color, null)
            frag1_area_user.buttonTintList = resources.getColorStateList(color, null)
        }

        frag1_showlist.setOnClickListener {
            hideKeyboard()

            if (!frag1_wifi.isChecked and !frag1_conv.isChecked and !frag1_custom.isChecked) {
                Toast.makeText(activity, "Select at least one table", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (frag1_conv.isChecked) {
                if (!frag1_conv_status1.isChecked and !frag1_conv_status2.isChecked and !frag1_conv_status3.isChecked) {
                    Toast.makeText(activity, "Select at least one store status", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (!frag1_conv_type1.isChecked and !frag1_conv_type2.isChecked and !frag1_conv_type3.isChecked and !frag1_conv_type4.isChecked) {
                    Toast.makeText(activity, "Select at least one store type", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
            if (frag1_area.isChecked and frag1_area_radius.text.toString().isEmpty()) {
                Toast.makeText(activity, "Radius cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var lat = 0.0
            var lng = 0.0
            var radius = -1.0
            if (frag1_area.isChecked) {
                if (frag1_area_current.isChecked) {
                    if (mylat == 9999.0 || mylng == 9999.0) {
                        Toast.makeText(activity, "No usable current coordinate", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    lat = mylat
                    lng = mylng
                }
                if (frag1_area_user.isChecked) {
                    if (userlat == 9999.0 || userlng == 9999.0) {
                        Toast.makeText(activity, "No user marker placed", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    lat = userlat
                    lng = userlng
                }
                radius = frag1_area_radius.text.toString().toDouble()
            }

            val table: Int = frag1_wifi.isChecked.int(0) + frag1_conv.isChecked.int(1) + frag1_custom.isChecked.int(2)
            val status: Int = frag1_conv_status1.isChecked.int(0) + frag1_conv_status2.isChecked.int(1) + frag1_conv_status3.isChecked.int(2)
            val type: Int = frag1_conv_type1.isChecked.int(0) + frag1_conv_type2.isChecked.int(1) + frag1_conv_type3.isChecked.int(2) + frag1_conv_type4.isChecked.int(3)

            itemlist = dbhelper.queryAdvance(table, frag1_name.text.toString(), frag1_addr.text.toString(), status, type, lat, lng, radius)

            if (itemlist.isEmpty()) {
                itemlist.add("0%no%No result found")
            }
            activity?.findViewById<RadioButton>(R.id.frag2)?.isChecked = true
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm: InputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(frag1_addr.windowToken, 0)
    }
}
