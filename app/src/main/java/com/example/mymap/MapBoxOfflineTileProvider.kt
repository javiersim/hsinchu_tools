package com.example.mymap

import android.database.sqlite.SQLiteClosable
import android.database.sqlite.SQLiteDatabase
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import kotlin.math.pow

class MapBoxOfflineTileProvider(pathToFile: String) : TileProvider, SQLiteClosable() {
    private var mMinimumZoom = Int.MIN_VALUE
    private var mMaximumZoom  = Int.MAX_VALUE
    private lateinit var mBounds: LatLngBounds
    private var mDatabase: SQLiteDatabase

    init {
        val flags = SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
        this.mDatabase = SQLiteDatabase.openDatabase(pathToFile, null, flags)
        this.calculateZoomConstraints()
        this.calculateBounds()
    }

    override fun onAllReferencesReleased() {
        TODO("Not yet implemented")
    }

    override fun getTile(p0: Int, p1: Int, p2: Int): Tile {
        var tile = TileProvider.NO_TILE
        if (this.isZoomLevelAvailable(p2) && this.isDatabaseAvailable()) {
            val projection = arrayOf("tile_data")
            val row = (2f.pow(p2) - p1).toInt() - 1
            val predicate = "tile_row = ? AND tile_column = ? AND zoom_level = ?"
            val values = arrayOf(row.toString(), p0.toString(), p2.toString())

            val cursor = this.mDatabase.query("tiles", projection, predicate, values, null, null, null)

            if (cursor!=null && cursor.count>0) {
                cursor.moveToFirst()
                if (!cursor.isAfterLast) {
                    tile = Tile(256, 256, cursor.getBlob(0))
                }
                cursor.close()
            }
        }
        return tile
    }

    override fun close() {
        super.close()
        this.mDatabase.close()
    }

    fun getMinimumZoom(): Int {
        return this.mMinimumZoom
    }

    fun getMaximumZoom(): Int {
        return this.mMaximumZoom
    }

    fun getBounds(): LatLngBounds {
        return this.mBounds
    }

    fun isZoomLevelAvailable(zoom: Int): Boolean {
        return (zoom >= this.mMinimumZoom) and (zoom <= this.mMaximumZoom)
    }

    private fun calculateZoomConstraints() {
        if (this.isDatabaseAvailable()) {
            val projection = arrayOf("value")
            val minArgs = arrayOf("minzoom")
            val maxArgs = arrayOf("maxzoom")

            var cursor = this.mDatabase.query("metadata", projection, "name = ?", minArgs, null, null, null)
            if (cursor!=null && cursor.count>0) {
                cursor.moveToFirst()
                if (!cursor.isAfterLast) {
                    this.mMinimumZoom = cursor.getInt(0)
                }
                cursor.close()
            }

            cursor = this.mDatabase.query("metadata", projection, "name = ?", maxArgs, null, null, null)
            if (cursor!=null && cursor.count>0) {
                cursor.moveToFirst()
                if (!cursor.isAfterLast) {
                    this.mMaximumZoom = cursor.getInt(0)
                }
                cursor.close()
            }
        }
    }

    private fun calculateBounds() {
        if (this.isDatabaseAvailable()) {
            val projection = arrayOf("value")
            val selectionArgs = arrayOf("bounds")

            val cursor = this.mDatabase.query("metadata", projection, "name = ?", selectionArgs, null, null, null)
            if (cursor!=null && cursor.count>0) {
                cursor.moveToFirst()
                if (!cursor.isAfterLast) {
                    val token = cursor.getString(0).split(",")

                    val w = token[0].toDouble()
                    val s = token[1].toDouble()
                    val e = token[2].toDouble()
                    val n = token[3].toDouble()

                    val ne = LatLng(n, e)
                    val sw = LatLng(s, w)

                    this.mBounds = LatLngBounds(sw, ne)
                }
                cursor.close()
            }
        }
    }

    private fun isDatabaseAvailable(): Boolean {
        return this.mDatabase.isOpen
    }
}