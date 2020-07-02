package com.example.mymap

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.location.*
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.File
import java.io.FileOutputStream

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var locMan: LocationManager
    private var loc: Location? = null
    private var markerUser: Marker? = null
    private var marker1SelectIndex = -1
    private var marker2SelectIndex = -1
    private var marker3SelectIndex = -1
    private lateinit var tileProvider: MapBoxOfflineTileProvider
    private lateinit var offlineOverlay: TileOverlay

    companion object {
        lateinit var dbhelper: Sqlite
        lateinit var mMap: GoogleMap
        var markerSet1: MutableList<Marker> = mutableListOf()
        var markerSet2: MutableList<Marker> = mutableListOf()
        var markerSet3: MutableList<Marker> = mutableListOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }else{
            initloc()
            initListener()
            dbhelper = Sqlite(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.selfq -> {
                val i = Intent(this, AdvanceActivity::class.java)
                i.putExtra("mylat", loc?.latitude)
                i.putExtra("mylng", loc?.longitude)
                i.putExtra("userlat", markerUser?.position?.latitude)
                i.putExtra("userlng", markerUser?.position?.longitude)
                i.putExtra("advance", advance.isChecked)
                startActivityForResult(i, 22)
                true
            }
            R.id.reset -> {
                lateinit var createInput: AlertDialog

                val option = arrayOf("wifi", "convenience", "custom")
                val status = booleanArrayOf(false, false, false)

                val confirm = AlertDialog.Builder(this)
                confirm.setCancelable(false)
                confirm.setPositiveButton("Confirm") { _, _ ->
                    if (status[0]) {
                        dbhelper.resetWifi()
                        genMarker1()
                        set1.toggle()
                        set1.toggle()
                    }
                    if (status[1]) {
                        dbhelper.resetConv()
                    }
                    if (status[2]) {
                        dbhelper.resetCustom()
                        genMarker2()
                        custom.toggle()
                        custom.toggle()
                    }
                }
                confirm.setNegativeButton("Cancel", null)

                val input = AlertDialog.Builder(this)
                input.setCancelable(false)
                input.setMultiChoiceItems(option, status) { _, which, isChecked ->
                    status[which] = isChecked
                    var temp = false
                    for (i in status) {
                        temp = temp or i
                    }
                    createInput.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = temp
                }

                input.setPositiveButton("Reset") { _, _ ->
                    var result = ""
                    for (i in status.indices) {
                        if (status[i]) {
                            result += " " + option[i] + ","
                        }
                    }
                    result = result.dropLast(1)
                    confirm.setMessage("Are you sure to reset these table:\n{$result }" + if(status[1]){"\n\n   ** this WILL take a while"}else{""})
                    confirm.show()
                }
                input.setNegativeButton("Cancel", null)
                createInput = input.create()
                createInput.show()
                createInput.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                true
            }
            R.id.offline -> {
                item.isChecked = !item.isChecked
                if (item.isChecked) {
                    mMap.mapType = GoogleMap.MAP_TYPE_NONE
                    offlineOverlay.isVisible = true
                    mMap.setMaxZoomPreference(15.0f)
                } else {
                    mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                    offlineOverlay.isVisible = false
                    mMap.setMaxZoomPreference(20.0f)
                }
                true
            }
            R.id.about -> {
                val i = Intent(this, AboutActivity::class.java)
                startActivity(i)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locMan.removeUpdates(loclistener)
        for (i in markerSet1.indices) {
            markerSet1[i].remove()
        }
        for (i in markerSet2.indices) {
            markerSet2[i].remove()
        }
        tileProvider.close()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            initloc()
            initListener()
            dbhelper = Sqlite(this)
        } else {
            Toast.makeText(this, "Location require to run this app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun moveOfflineTile() {
        val outputFile = File(applicationInfo.dataDir + "/databases/taiwan_emap6.mbtiles")
        if (outputFile.exists()) {
            return
        }

        val input = resources.openRawResource(R.raw.taiwan_emap6)
        outputFile.parentFile.mkdirs()
        outputFile.createNewFile()
        val output = FileOutputStream(outputFile, false)

        val buffer = ByteArray(1024)
        var nread = input.read(buffer)
        while(nread > 0) {
            output.write(buffer, 0, nread)
            nread = input.read(buffer)
        }

        input.close()
        output.close()
    }

    private fun initListener() {
        set1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                for (item in markerSet1) {
                    item.isVisible = true
                }
            } else {
                for (item in markerSet1) {
                    item.isVisible = false
                }
            }
            add.visibility = View.GONE
            remove.visibility = View.GONE
            delete.visibility = View.GONE
        }

        custom.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                for (item in markerSet2) {
                    item.isVisible = true
                }
            } else {
                for (item in markerSet2) {
                    item.isVisible = false
                }
            }
            add.visibility = View.GONE
            remove.visibility = View.GONE
            delete.visibility = View.GONE
        }

        advance.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                for (item in markerSet3) {
                    item.isVisible = true
                }
            } else {
                for (item in markerSet3) {
                    item.isVisible = false
                }
            }
            add.visibility = View.GONE
            remove.visibility = View.GONE
            delete.visibility = View.GONE
        }

        add.setOnClickListener {
            val input = AlertDialog.Builder(this)
            input.setCancelable(false)

            val content = LayoutInflater.from(this).inflate(R.layout.add_marker, null, false)

            input.setView(content)
            input.setPositiveButton("Confirm") { _, _ ->
                val name = content.findViewById<TextView>(R.id.add_name).text.toString()
                val addr = content.findViewById<TextView>(R.id.add_addr).text.toString()
                val lat = markerUser?.position?.latitude!!
                val lng = markerUser?.position?.longitude!!
                val note = content.findViewById<TextView>(R.id.add_note).text.toString()
                if (name.isEmpty()) {
                    Toast.makeText(this, "Empty name", Toast.LENGTH_SHORT).show()
                } else if (addr.isEmpty()) {
                    Toast.makeText(this, "Empty address", Toast.LENGTH_SHORT).show()
                } else {
                    val rowid = dbhelper.insertCustom(name, addr, note, lat, lng)
                    val snip = "編號: " + rowid + "\n地址: " + addr + "\n備註: " + note
                    markerSet2.add(mMap.addMarker(MarkerOptions().position(LatLng(lat, lng)).title(name).snippet(snip).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))))
                    markerSet2.last().isVisible = custom.isChecked
                    markerUser!!.remove()
                    add.visibility = View.GONE
                    Toast.makeText(this, "New marker added", Toast.LENGTH_SHORT).show()
                }
            }
            input.setNegativeButton("Cancel", null)
            input.show()
        }

        remove.setOnClickListener {
            val confirm = AlertDialog.Builder(this)
            confirm.setCancelable(false)
            confirm.setTitle("Are you sure to remove this marker?")
            confirm.setMessage("This will only remove marker from the set and not delete from the database.")
            confirm.setPositiveButton("Remove") { _, _ ->
                markerSet3[marker3SelectIndex].remove()
                markerSet3.removeAt(marker3SelectIndex)
                remove.visibility = View.GONE
            }
            confirm.setNegativeButton("Cancel", null)
            confirm.show()
        }

        delete.setOnClickListener {
            val confirm = AlertDialog.Builder(this)
            confirm.setCancelable(false)
            confirm.setMessage("Are you sure to delete this entry?")
            confirm.setPositiveButton("Delete") { _, _ ->
                if (marker2SelectIndex < 0) {
                    dbhelper.deleteWifi(seperate(markerSet1[marker1SelectIndex].snippet, "編號"))
                    markerSet1[marker1SelectIndex].remove()
                    markerSet1.removeAt(marker1SelectIndex)
                    delete.visibility = View.GONE
                } else {
                    dbhelper.deleteCustom(seperate(markerSet2[marker2SelectIndex].snippet, "編號"))
                    markerSet2[marker2SelectIndex].remove()
                    markerSet2.removeAt(marker2SelectIndex)
                    delete.visibility = View.GONE
                }
            }
            confirm.setNegativeButton("Cancel", null)
            confirm.show()
        }
    }

    private fun initloc() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locMan = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        val provider: String = locMan.getBestProvider(criteria, true)
        try {
            locMan.requestLocationUpdates(provider, 1000, 1f, loclistener)
        } catch (e: SecurityException) {
            Toast.makeText(this, "Error: initloc -> requestLocationUpdate", Toast.LENGTH_SHORT).show()
        }
    }

    private var loclistener = object: LocationListener{
        override fun onLocationChanged(location: Location?) {
            if (location != null) {
                loc = location
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            TODO("Not yet implemented")
        }

        override fun onProviderEnabled(provider: String?) {
            TODO("Not yet implemented")
        }

        override fun onProviderDisabled(provider: String?) {
            TODO("Not yet implemented")
        }
    }

    private fun genMarker1() {
        for (item in markerSet1) {
            item.remove()
        }
        markerSet1.clear()
        //編號,機關構名稱,熱點名稱,熱點類別,縣市名稱,鄉鎮市區,地址,緯度,經度,郵遞區號,cos緯度,sin緯度,cos經度,sin經度
        for (i in dbhelper.queryAllWifi()) {
            val temp = i.split("%".toRegex())
            val snip = "編號: " + temp[0] + "\n機關構名稱: " + temp[1] + "\n熱點類別: " + temp[3] + "\n縣市名稱: " + temp[4] + "\n鄉鎮市區: " + temp[5] + "\n地址: " + temp[6]
            markerSet1.add(mMap.addMarker(MarkerOptions().position(LatLng(temp[7].toDouble(), temp[8].toDouble())).title(temp[2]).snippet(snip).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))))
        }
        for (item in markerSet1) {
            item.isVisible = false
        }
    }

    private fun genMarker2() {
        for (item in markerSet2) {
            item.remove()
        }
        markerSet2.clear()
        //名稱,地址,備註,緯度,經度,cos緯度,sin緯度,cos經度,sin經度 + rowid
        for (i in dbhelper.queryAllCustom()) {
            val temp = i.split("%".toRegex())
            val snip = "編號: " + temp[9] + "\n地址: " + temp[1] + "\n備註: " + temp[2]
            markerSet2.add(mMap.addMarker(MarkerOptions().position(LatLng(temp[3].toDouble(), temp[4].toDouble())).title(temp[0]).snippet(snip).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))))
        }
        for (item in markerSet2) {
            item.isVisible = false
        }
    }

    private fun seperate(snip: String, key: String): String {
        for (i in snip.split("\n".toRegex())) {
            val temp = i.split(": ".toRegex(), 2)
            if (temp[0] == key) {
                return temp[1]
            }
        }
        return ""
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        mMap.setMinZoomPreference(8.0f)
        mMap.setMaxZoomPreference(20.0f)

        moveOfflineTile()
        tileProvider = MapBoxOfflineTileProvider(applicationInfo.dataDir + "/databases/taiwan_emap6.mbtiles")
        offlineOverlay = mMap.addTileOverlay(TileOverlayOptions().tileProvider(tileProvider))
        offlineOverlay.isVisible = false

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        try {
            val temp = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(temp.latitude, temp.longitude), 15f))
        } catch (e: SecurityException) {
            Toast.makeText(this, "Error: onMapReady -> moveCamera", Toast.LENGTH_SHORT).show()
        }

        genMarker1()
        genMarker2()
        mMap.setOnMapLongClickListener {
            add.visibility = View.GONE
            remove.visibility = View.GONE
            delete.visibility = View.GONE
            if (markerUser == null) {
                markerUser = mMap.addMarker(MarkerOptions().title("user").position(it))
            } else {
                val temp = markerUser!!.position
                markerUser!!.remove()
                markerUser = null
                if (temp != it) {
                    markerUser = mMap.addMarker(MarkerOptions().title("user").position(it))
                }
            }
        }

        mMap.setOnMapClickListener {
            add.visibility = View.GONE
            remove.visibility = View.GONE
            delete.visibility = View.GONE
            if (markerUser != null) {
                markerUser!!.remove()
                markerUser = null
            }
        }

        mMap.setOnMarkerClickListener { marker ->
            marker1SelectIndex = markerSet1.indexOf(marker)
            marker2SelectIndex = markerSet2.indexOf(marker)
            marker3SelectIndex = markerSet3.indexOf(marker)

            if (marker == markerUser) {
                add.visibility = View.VISIBLE
                remove.visibility = View.GONE
                delete.visibility = View.GONE
            } else if (marker3SelectIndex < 0) {
                add.visibility = View.GONE
                remove.visibility = View.GONE
                delete.visibility = View.VISIBLE
            } else {
                add.visibility = View.GONE
                remove.visibility = View.VISIBLE
                delete.visibility = View.GONE
            }

            false
        }

        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoContents(p0: Marker?): View {
                TODO("Not yet implemented")
            }

            override fun getInfoWindow(p0: Marker?): View {
                val info = LinearLayout(this@MapsActivity)
                info.orientation = LinearLayout.VERTICAL
                info.setBackgroundResource(R.drawable.info_window)

                val title = TextView(this@MapsActivity)
                title.setTextColor(Color.BLACK)
                title.gravity = Gravity.CENTER
                title.setTypeface(null, Typeface.BOLD)
                title.text = p0?.title

                val snippet = TextView(this@MapsActivity)
                snippet.setTextColor(Color.GRAY)
                snippet.text = p0?.snippet

                info.addView(title)
                info.addView(snippet)
                return info
            }
        })
    }
}
