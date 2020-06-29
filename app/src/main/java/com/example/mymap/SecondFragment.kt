package com.example.mymap

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.mymap.MapsActivity.Companion.dbhelper
import com.example.mymap.MapsActivity.Companion.mMap
import com.example.mymap.MapsActivity.Companion.markerSet1
import com.example.mymap.MapsActivity.Companion.markerSet2
import com.example.mymap.MapsActivity.Companion.markerSet3
import com.example.mymap.AdvanceActivity.Companion.itemlist
import com.example.mymap.AdvanceActivity.Companion.advance
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_second.*

class SecondFragment : Fragment() {

    lateinit var adapter: MyAdapter

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (!hidden) {
            adapter = MyAdapter(requireView().context, android.R.layout.simple_list_item_1, itemlist)
            listview.adapter = adapter
            setItemClick(itemlist[0].split("%".toRegex())[1] != "no")
        }
    }

    private fun setItemClick(status: Boolean) {
        if (status) {
            listview.setOnItemClickListener { parent, view, position, id ->
                val detail = AlertDialog.Builder(activity)
                detail.setCancelable(false)
                detail.setTitle("Entry Detail")
                val message: String
                when (itemlist[position].split("%".toRegex())[1]) {
                    "wifi" -> message = wifiFormat(dbhelper.queryKeyWifi(itemlist[position].split("%".toRegex())[0])[0])
                    "conv" -> message = convFormat(dbhelper.queryKeyConv(itemlist[position].split("%".toRegex())[0])[0])
                    "custom" -> message = customFormat(dbhelper.queryKeyCustom(itemlist[position].split("%".toRegex())[0])[0])
                    else -> message = "error"
                }
                detail.setMessage(message)
                if (message != "error") {
                    detail.setPositiveButton("Add marker") { _, _ ->
                        val snip = "type: " + itemlist[position].split("%".toRegex())[1] + "\n" + message
                        markerSet3.add(mMap.addMarker(
                            MarkerOptions().position(LatLng(itemlist[position].split("%".toRegex())[4].toDouble(), itemlist[position].split("%".toRegex())[5].toDouble())).title("From advance query").snippet(snip).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))))
                        markerSet3.last().isVisible = advance
                        Toast.makeText(activity, "New marker added to advance set", Toast.LENGTH_SHORT).show()
                    }

                    val confirm = AlertDialog.Builder(activity)
                    confirm.setCancelable(false)
                    confirm.setTitle("Are you sure to DELETE this entry?")
                    confirm.setMessage(message)
                    confirm.setPositiveButton("Confirm") { _, _ ->
                        when (itemlist[position].split("%".toRegex())[1]) {
                            "wifi" -> {
                                val key = itemlist[position].split("%".toRegex())[0]
                                deleteInMarkerSet1(key)
                                deleteInMarkerSet3("wifi", key)
                                dbhelper.deleteWifi(key)
                                itemlist.removeAt(position)
                                if (itemlist.isEmpty()) {
                                    itemlist.add("0%no%Try search new thing")
                                    listview.onItemClickListener = null
                                }
                                adapter.notifyDataSetChanged()
                            }
                            "conv" -> {
                                val key = itemlist[position].split("%".toRegex())[0]
                                deleteInMarkerSet3("conv", key)
                                dbhelper.deleteConv(key)
                                itemlist.removeAt(position)
                                if (itemlist.isEmpty()) {
                                    itemlist.add("0%no%Try search new thing")
                                    listview.onItemClickListener = null
                                }
                                adapter.notifyDataSetChanged()
                            }
                            "custom" -> {
                                val key = itemlist[position].split("%".toRegex())[0]
                                deleteInMarkerSet2(key)
                                deleteInMarkerSet3("custom", key)
                                dbhelper.deleteCustom(itemlist[position].split("%".toRegex())[0])
                                itemlist.removeAt(position)
                                if (itemlist.isEmpty()) {
                                    itemlist.add("0%no%Try search new thing")
                                    listview.onItemClickListener = null
                                }
                                adapter.notifyDataSetChanged()
                            }
                            else -> Toast.makeText(activity, "error", Toast.LENGTH_SHORT).show()
                        }
                    }
                    confirm.setNegativeButton("Cancel", null)

                    detail.setNegativeButton("Delete entry") { _, _ ->
                        confirm.show()
                    }
                }
                detail.setNeutralButton("Cancel", null)
                detail.show()
            }
        } else {
            listview.onItemClickListener = null
        }
    }

    private fun wifiFormat(message: String): String {
        val token = message.split("%".toRegex(), 9)
        return "編號: " + token[0] + "\n機關構名稱: " + token[1] +
                "\n熱點名稱: " + token[2] + "\n熱點類別: " + token[3] +
                "\n縣市名稱: " + token[4] + "\n鄉鎮市區: " + token[5] +
                "\n地址: " + token[6] + "\n緯度: " + token[7] +
                "\n經度: " + token[8]
    }

    private fun convFormat(message: String): String {
        val token = message.split("%".toRegex(), 7)
        return "公司統一編號: " + token[0] + "\n分公司統一編號: " + token[1] +
                "\n分公司名稱: " + token[2] + "\n分公司地址: " + token[3] +
                "\n分公司狀態: " + token[4] + "\n緯度: " + token[5] +
                "\n經度: " + token[6]
    }

    private fun customFormat(message: String): String {
        val token = message.split("%".toRegex(), 6)
        return "編號: " + token[5] + "\n名稱: " + token[0] +
                "\n備註: " + token[1] + "\n地址: " + token[2] +
                "\n緯度: " + token[3] + "\n經度: " + token[4]
    }

    private fun deleteInMarkerSet1(key: String) {
        for (i in markerSet1.indices) {
            if (key == markerSet1[i].snippet.split("\n".toRegex())[0].split(": ".toRegex(), 2)[1]) {
                markerSet1[i].remove()
                markerSet1.removeAt(i)
                break
            }
        }
    }

    private fun deleteInMarkerSet2(key: String) {
        for (i in markerSet2.indices) {
            if (key == markerSet2[i].snippet.split("\n".toRegex())[0].split(": ".toRegex(), 2)[1]) {
                markerSet2[i].remove()
                markerSet2.removeAt(i)
                break
            }
        }
    }

    private fun deleteInMarkerSet3(type: String, key: String) {
        for (i in markerSet3.indices) {
            if (type == markerSet3[i].snippet.split("\n".toRegex())[0].split(": ".toRegex(), 2)[1] &&
                key == markerSet3[i].snippet.split("\n".toRegex())[1].split(": ".toRegex(), 2)[1]) {
                markerSet3[i].remove()
                markerSet3.removeAt(i)
                break
            }
        }
    }
}
