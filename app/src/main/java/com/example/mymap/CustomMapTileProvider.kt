package com.example.mymap

import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileProvider
import java.io.*

// For tile as png, not used in this project
class CustomMapTileProvider: TileProvider {
    private val TILE_WIDTH = 256
    private val TILE_HEIGHT = 256
    private val BUFFER_SIZE = 16 * 1024

    override fun getTile(p0: Int, p1: Int, p2: Int): Tile? {
        val image: ByteArray? = readTileImage(p0, p1, p2)
        if (image == null) {
            return null
        } else {
            return Tile(TILE_WIDTH, TILE_HEIGHT, image)
        }
    }

    private fun readTileImage(x: Int, y: Int, zoom: Int): ByteArray? {
        var inputStream: InputStream? = null
        var buffer: ByteArrayOutputStream? = null

        try {
            inputStream = this.javaClass.classLoader?.getResourceAsStream("res/raw/${getTileFilename(x, y, zoom)}")
            buffer = ByteArrayOutputStream()

            var nRead: Int = 0
            var data: ByteArray = ByteArray(BUFFER_SIZE)
            while (true) {
                nRead = inputStream!!.read(data, 0, BUFFER_SIZE)
                if (nRead == -1) break
                buffer.write(data, 0, nRead)
            }
            buffer.flush()
            return buffer.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            return null
        } finally {
            if (inputStream != null) try { inputStream.close() } catch (ignored: Exception) {}
            if (buffer != null) try { buffer.close() } catch (ignored: Exception) {}
        }
    }

    private fun getTileFilename(x: Int, y: Int, zoom: Int): String {
        return "z" + zoom + "_" + x + "_" + y + "_r.png"
    }
}