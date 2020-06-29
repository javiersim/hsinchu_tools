package com.example.mymap

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.acos

@RequiresApi(Build.VERSION_CODES.P)
class Sqlite(
    context: MapsActivity,
    name: String = "my.db",
    factory: SQLiteDatabase.CursorFactory? = null,
    version: Int = 4
) : SQLiteOpenHelper(context, name, factory, version) {
    private val tableName1: String = "wifi" //編號,機關構名稱,熱點名稱,熱點類別,縣市名稱,鄉鎮市區,地址,緯度,經度,郵遞區號,cos緯度,sin緯度,cos經度,sin經度
    private val tableName2: String = "conv" //公司統一編號,分公司統一編號,分公司名稱,分公司地址,分公司狀態,緯度,經度,cos緯度,sin緯度,cos經度,sin經度
    private val tableName3: String = "custom" //名稱,地址,備註,緯度,經度,cos緯度,sin緯度,cos經度,sin經度
    private val tableName0: String = "convname" //公司統一編號,公司名稱,店名

    override fun onCreate(db: SQLiteDatabase) {
        val sql0: String = "CREATE TABLE IF NOT EXISTS " + tableName0 + "(" +
                "公司統一編號 int PRIMARY KEY, " +
                "公司名稱 longtext NOT NULL, " +
                "店名 longtext NOT NULL " +
                ");"
        db.execSQL(sql0)
        importconvnamedata(db)
        val sql1: String = "CREATE TABLE IF NOT EXISTS " + tableName1 + "(" +
                "編號 int PRIMARY KEY, " +
                "機關構名稱 longtext, " +
                "熱點名稱 longtext NOT NULL, " +
                "熱點類別 longtext, " +
                "縣市名稱 longtext, " +
                "鄉鎮市區 longtext, " +
                "地址 longtext NOT NULL, " +
                "緯度 double NOT NULL, " +
                "經度 double NOT NULL, " +
                "cos緯度 double NOT NULL, " +
                "sin緯度 double NOT NULL, " +
                "cos經度 double NOT NULL, " +
                "sin經度 double NOT NULL " +
                ");"
        db.execSQL(sql1)
        importwifidata(db)
        val sql2: String = "CREATE TABLE IF NOT EXISTS " + tableName2 + "(" +
                "公司統一編號 int, " +
                "分公司統一編號 int PRIMARY KEY, " +
                "分公司名稱 longtext, " +
                "分公司地址 longtext NOT NULL, " +
                "分公司狀態 int, " +
                "緯度 double NOT NULL, " +
                "經度 double NOT NULL," +
                "cos緯度 double NOT NULL, " +
                "sin緯度 double NOT NULL, " +
                "cos經度 double NOT NULL, " +
                "sin經度 double NOT NULL, " +
                "FOREIGN KEY(公司統一編號) REFERENCES convname(公司統一編號) " +
                ");"
        db.execSQL(sql2)
        importconvdata(db)
        val sql3: String = "CREATE TABLE IF NOT EXISTS " + tableName3 + "(" +
                "名稱 longtext NOT NULL, " +
                "地址 longtext NOT NULL, " +
                "備註 longtext, " +
                "緯度 double NOT NULL, " +
                "經度 double NOT NULL, " +
                "cos緯度 double NOT NULL, " +
                "sin緯度 double NOT NULL, " +
                "cos經度 double NOT NULL, " +
                "sin經度 double NOT NULL " +
                ");"
        db.execSQL(sql3)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    private fun importwifidata(db: SQLiteDatabase) {
        val inputStream = this.javaClass.classLoader?.getResourceAsStream("res/raw/wifi.csv")
        val file = InputStreamReader(inputStream)
        val buffer = BufferedReader(file)
        buffer.readLine()
        while (true) {
            val line = buffer.readLine() ?: break
            val str = line.split(",".toRegex(), 13).toMutableList()
            val values = ContentValues()
            values.put("編號", str[0])
            values.put("機關構名稱", str[1])
            values.put("熱點名稱", str[2])
            values.put("熱點類別", str[3])
            values.put("縣市名稱", str[4])
            values.put("鄉鎮市區", str[5])
            values.put("地址", str[6])
            values.put("緯度", str[7])
            values.put("經度", str[8])
            values.put("cos緯度", str[9])
            values.put("sin緯度", str[10])
            values.put("cos經度", str[11])
            values.put("sin經度", str[12])
            db.insert(tableName1, null, values)
        }
        buffer.close()
    }

    fun queryAllWifi(): MutableList<String> {
        val results = mutableListOf<String>()
        val query = "SELECT * " +
                "FROM " + tableName1 + ";"
        val cursor = readableDatabase.rawQuery(query, null)
        if (cursor!=null && cursor.count>0) {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                results.add(cursor.getString(cursor.getColumnIndex("編號")) + "%" +
                        cursor.getString(cursor.getColumnIndex("機關構名稱")) + "%" +
                        cursor.getString(cursor.getColumnIndex("熱點名稱")) + "%" +
                        cursor.getString(cursor.getColumnIndex("熱點類別")) + "%" +
                        cursor.getString(cursor.getColumnIndex("縣市名稱")) + "%" +
                        cursor.getString(cursor.getColumnIndex("鄉鎮市區")) + "%" +
                        cursor.getString(cursor.getColumnIndex("地址")) + "%" +
                        cursor.getString(cursor.getColumnIndex("緯度")) + "%" +
                        cursor.getString(cursor.getColumnIndex("經度")) + "%" +
                        cursor.getString(cursor.getColumnIndex("cos緯度")) + "%" +
                        cursor.getString(cursor.getColumnIndex("sin緯度")) + "%" +
                        cursor.getString(cursor.getColumnIndex("cos經度")) + "%" +
                        cursor.getString(cursor.getColumnIndex("sin經度")))
                cursor.moveToNext()
            }
            cursor.close()
        }
        return results
    }

    fun queryKeyWifi(key: String): MutableList<String> {
        val results = mutableListOf<String>()
        val query = "SELECT * " +
                "FROM " + tableName1 + " " +
                "WHERE 編號 = " + key + ";"
        val cursor = readableDatabase.rawQuery(query, null)
        if (cursor!=null && cursor.count>0) {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                results.add(cursor.getString(cursor.getColumnIndex("編號")) + "%" +
                        cursor.getString(cursor.getColumnIndex("機關構名稱")) + "%" +
                        cursor.getString(cursor.getColumnIndex("熱點名稱")) + "%" +
                        cursor.getString(cursor.getColumnIndex("熱點類別")) + "%" +
                        cursor.getString(cursor.getColumnIndex("縣市名稱")) + "%" +
                        cursor.getString(cursor.getColumnIndex("鄉鎮市區")) + "%" +
                        cursor.getString(cursor.getColumnIndex("地址")) + "%" +
                        cursor.getString(cursor.getColumnIndex("緯度")) + "%" +
                        cursor.getString(cursor.getColumnIndex("經度")))
                cursor.moveToNext()
            }
            cursor.close()
        }
        return results
    }

    private fun importconvnamedata(db: SQLiteDatabase) {
        val inputStream = this.javaClass.classLoader?.getResourceAsStream("res/raw/convname.csv")
        val file = InputStreamReader(inputStream)
        val buffer = BufferedReader(file)
        buffer.readLine()
        while (true) {
            val line = buffer.readLine() ?: break
            val str = line.split(",".toRegex(), 3)
            val values = ContentValues()
            values.put("公司統一編號", str[0])
            values.put("公司名稱", str[1])
            values.put("店名", str[2])
            db.insert(tableName0, null, values)
        }
        buffer.close()
    }

    private fun importconvdata(db: SQLiteDatabase) {
        val inputStream = this.javaClass.classLoader?.getResourceAsStream("res/raw/convenience.csv")
        val file = InputStreamReader(inputStream)
        val buffer = BufferedReader(file)
        buffer.readLine()
        while (true) {
            val line = buffer.readLine() ?: break
            val str = line.split(",".toRegex(), 11)
            val values = ContentValues()
            values.put("公司統一編號", str[0])
            values.put("分公司統一編號", str[1])
            values.put("分公司名稱", str[2])
            values.put("分公司地址", str[3])
            values.put("分公司狀態", str[4])
            values.put("緯度", str[5])
            values.put("經度", str[6])
            values.put("cos緯度", str[7])
            values.put("sin緯度", str[8])
            values.put("cos經度", str[9])
            values.put("sin經度", str[10])
            db.insert(tableName2, null, values)
        }
        buffer.close()
    }

    fun queryKeyConv(key: String): MutableList<String> {
        val results = mutableListOf<String>()
        val query = "SELECT * " +
                "FROM " + tableName2 + " " +
                "WHERE 分公司統一編號 = " + key + ";"
        val cursor = readableDatabase.rawQuery(query, null)
        if (cursor!=null && cursor.count>0) {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                results.add(cursor.getString(cursor.getColumnIndex("公司統一編號")) + "%" +
                        cursor.getString(cursor.getColumnIndex("分公司統一編號")) + "%" +
                        cursor.getString(cursor.getColumnIndex("分公司名稱")) + "%" +
                        cursor.getString(cursor.getColumnIndex("分公司地址")) + "%" +
                        cursor.getString(cursor.getColumnIndex("分公司狀態")) + "%" +
                        cursor.getString(cursor.getColumnIndex("緯度")) + "%" +
                        cursor.getString(cursor.getColumnIndex("經度")))
                cursor.moveToNext()
            }
            cursor.close()
        }
        return results
    }

    fun queryAllCustom(): MutableList<String> {
        val results = mutableListOf<String>()
        val query = "SELECT * , rowid " +
                "FROM " + tableName3 + ";"
        val cursor = readableDatabase.rawQuery(query, null)
        if (cursor!=null && cursor.count>0) {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                results.add(cursor.getString(cursor.getColumnIndex("名稱")) + "%" +
                        cursor.getString(cursor.getColumnIndex("地址")) + "%" +
                        cursor.getString(cursor.getColumnIndex("備註")) + "%" +
                        cursor.getString(cursor.getColumnIndex("緯度")) + "%" +
                        cursor.getString(cursor.getColumnIndex("經度")) + "%" +
                        cursor.getString(cursor.getColumnIndex("cos緯度")) + "%" +
                        cursor.getString(cursor.getColumnIndex("sin緯度")) + "%" +
                        cursor.getString(cursor.getColumnIndex("cos經度")) + "%" +
                        cursor.getString(cursor.getColumnIndex("sin經度")) + "%" +
                        cursor.getString(cursor.getColumnIndex("rowid")))
                cursor.moveToNext()
            }
            cursor.close()
        }
        return results
    }

    fun queryKeyCustom(key: String): MutableList<String> {
        val results = mutableListOf<String>()
        val query = "SELECT * , rowid " +
                "FROM " + tableName3 + " " +
                "WHERE rowid = " + key + ";"
        val cursor = readableDatabase.rawQuery(query, null)
        if (cursor!=null && cursor.count>0) {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                results.add(cursor.getString(cursor.getColumnIndex("名稱")) + "%" +
                        cursor.getString(cursor.getColumnIndex("備註")) + "%" +
                        cursor.getString(cursor.getColumnIndex("地址")) + "%" +
                        cursor.getString(cursor.getColumnIndex("緯度")) + "%" +
                        cursor.getString(cursor.getColumnIndex("經度")) + "%" +
                        cursor.getString(cursor.getColumnIndex("rowid")))
                cursor.moveToNext()
            }
            cursor.close()
        }
        return results
    }

    fun queryAdvance(table: Int, name: String, addr: String, convstatus: Int, convtype: Int, lat: Double, lng: Double, radius: Double): MutableList<String> {
        val results = mutableListOf<String>()

        var subquery = ""
        if (table and 0b001 > 0) {
            subquery += genWifiQuery(name, addr)
        }
        if (table and 0b010 > 0) {
            if (subquery.isEmpty()) {
                subquery += genConvQuery(addr, convstatus, convtype)
            } else {
                subquery += " UNION " + genConvQuery(addr, convstatus, convtype)
            }
        }
        if (table and 0b100 > 0) {
            if (subquery.isEmpty()) {
                subquery += genCustomQuery(name, addr)
            } else {
                subquery += " UNION " + genCustomQuery(name, addr)
            }
        }

        val query: String

        if (radius != -1.0) {
            val curCosLat = cos(lat*PI/180)
            val curSinLat = sin(lat*PI/180)
            val curCosLng = cos(lng*PI/180)
            val curSinLng = sin(lng*PI/180)
            val cosAllowedDistance = cos(radius/6371.0)

            query = "SELECT key, type, name, addr, 緯度, 經度, ($curSinLat * sin緯度 + $curCosLat * cos緯度 * (cos經度 * $curCosLng + sin經度 * $curSinLng)) as cos_dist " +
                    "FROM ($subquery) " +
                    "WHERE cos_dist > $cosAllowedDistance " +
                    "order by cos_dist desc;"
        } else {
            query = "SELECT key, type, name, addr, 緯度, 經度 " +
                    "FROM ($subquery);"
        }

        val cursor = readableDatabase.rawQuery(query, null)
        if (cursor!=null && cursor.count>0) {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                results.add(cursor.getString(cursor.getColumnIndex("key")) + "%" +
                        cursor.getString(cursor.getColumnIndex("type")) + "%" +
                        cursor.getString(cursor.getColumnIndex("name")) + "%" +
                        cursor.getString(cursor.getColumnIndex("addr")) + "%" +
                        cursor.getString(cursor.getColumnIndex("緯度")) + "%" +
                        cursor.getString(cursor.getColumnIndex("經度")) +
                        if (radius!=-1.0) {
                            "%" + (acos(cursor.getDouble(cursor.getColumnIndex("cos_dist")))*6371.0)
                        } else { "" })
                cursor.moveToNext()
            }
            cursor.close()
        }
        return results
    }

    private fun genWifiQuery(name: String, addr: String): String {
        return "SELECT 編號 as key, 'wifi' as type,  熱點名稱 as name, 地址 as addr, 緯度, 經度, cos緯度, sin緯度, cos經度, sin經度 " +
                "FROM " + tableName1 + " " +
                "WHERE 熱點名稱 LIKE '%" + name + "%' AND " +
                "地址 LIKE '%" + addr + "%' "
    }

    private fun genConvQuery(addr: String, status: Int, type: Int): String {
        var query = "SELECT 分公司統一編號 as key, 'conv' as type,  t2.店名 as name, t1.分公司地址 as addr, 緯度, 經度, cos緯度, sin緯度, cos經度, sin經度 " +
                    "FROM " + tableName2 + " as t1, " + tableName0 + " as t2 " +
                    "WHERE t1.公司統一編號 = t2.公司統一編號 AND " +
                    "t1.分公司地址 LIKE '%" + addr + "%' "

        var statusq = ""
        if (status and 0b001 > 0) {
            statusq += "AND (t1.分公司狀態=1 "
        }
        if (status and 0b010 > 0) {
            if (statusq.isEmpty()) {
                statusq += "AND (t1.分公司狀態=2 "
            } else {
                statusq += "OR t1.分公司狀態=2 "
            }
        }
        if (status and 0b100 > 0) {
            if (statusq.isEmpty()) {
                statusq += "AND (t1.分公司狀態=3 "
            } else {
                statusq += "OR t1.分公司狀態=3 "
            }
        }
        if (statusq.isNotEmpty()) {
            query += statusq + ") "
        }

        var typeq = ""
        if (type and 0b0001 > 0) {
            typeq += "AND (t1.公司統一編號=22555003 "
        }
        if (type and 0b0010 > 0) {
            if (typeq.isEmpty()) {
                typeq += "AND (t1.公司統一編號=23060248 "
            } else {
                typeq += "OR t1.公司統一編號=23060248 "
            }
        }
        if (type and 0b0100 > 0) {
            if (typeq.isEmpty()) {
                typeq += "AND (t1.公司統一編號=23285582 "
            } else {
                typeq += "OR t1.公司統一編號=23285582 "
            }
        }
        if (type and 0b1000 > 0) {
            if (typeq.isEmpty()) {
                typeq += "AND (t1.公司統一編號=22853565 "
            } else {
                typeq += "OR t1.公司統一編號=22853565 "
            }
        }
        if (typeq.isNotEmpty()) {
            query += typeq + ") "
        }
        return query
    }

    private fun genCustomQuery(name: String, addr: String): String {
        return "SELECT rowid as key, 'custom' as type,  名稱 as name, 地址 as addr, 緯度, 經度, cos緯度, sin緯度, cos經度, sin經度 " +
                "FROM " + tableName3 + " " +
                "WHERE 名稱 LIKE '%" + name + "%' AND " +
                "地址 LIKE '%" + addr + "%' "
    }

    fun deleteWifi(key: String) {
        val sql = "DELETE FROM $tableName1 WHERE 編號=$key;"
        writableDatabase.execSQL(sql)
    }

    fun deleteConv(key: String) {
        val sql = "DELETE FROM $tableName2 WHERE 分公司統一編號=$key;"
        writableDatabase.execSQL(sql)
    }

    fun deleteCustom(key: String) {
        val sql = "DELETE FROM $tableName3 WHERE rowid=$key;"
        writableDatabase.execSQL(sql)
    }

    fun resetWifi() {
        val sql1 = "DROP TABLE IF EXISTS '$tableName1';"
        writableDatabase.execSQL(sql1)
        val sql2: String = "CREATE TABLE IF NOT EXISTS " + tableName1 + "(" +
                "編號 int PRIMARY KEY, " +
                "機關構名稱 longtext, " +
                "熱點名稱 longtext NOT NULL, " +
                "熱點類別 longtext, " +
                "縣市名稱 longtext, " +
                "鄉鎮市區 longtext, " +
                "地址 longtext NOT NULL, " +
                "緯度 double NOT NULL, " +
                "經度 double NOT NULL, " +
                "cos緯度 double NOT NULL, " +
                "sin緯度 double NOT NULL, " +
                "cos經度 double NOT NULL, " +
                "sin經度 double NOT NULL " +
                ");"
        writableDatabase.execSQL(sql2)
        importwifidata(this.writableDatabase)
    }

    fun resetConv() {
        val sql1 = "DROP TABLE IF EXISTS '$tableName2';"
        writableDatabase.execSQL(sql1)
        val sql2: String = "CREATE TABLE IF NOT EXISTS " + tableName2 + "(" +
                "公司統一編號 int, " +
                "分公司統一編號 int PRIMARY KEY, " +
                "分公司名稱 longtext, " +
                "分公司地址 longtext NOT NULL, " +
                "分公司狀態 int, " +
                "緯度 double NOT NULL, " +
                "經度 double NOT NULL," +
                "cos緯度 double NOT NULL, " +
                "sin緯度 double NOT NULL, " +
                "cos經度 double NOT NULL, " +
                "sin經度 double NOT NULL, " +
                "FOREIGN KEY(公司統一編號) REFERENCES convname(公司統一編號) " +
                ");"
        writableDatabase.execSQL(sql2)
        importconvdata(this.writableDatabase)
    }

    fun resetCustom() {
        val sql1 = "DROP TABLE IF EXISTS '$tableName3';"
        writableDatabase.execSQL(sql1)
        val sql2: String = "CREATE TABLE IF NOT EXISTS " + tableName3 + "(" +
                "名稱 longtext NOT NULL, " +
                "地址 longtext NOT NULL, " +
                "備註 longtext, " +
                "緯度 double NOT NULL, " +
                "經度 double NOT NULL, " +
                "cos緯度 double NOT NULL, " +
                "sin緯度 double NOT NULL, " +
                "cos經度 double NOT NULL, " +
                "sin經度 double NOT NULL " +
                ");"
        writableDatabase.execSQL(sql2)
    }

    fun insertCustom(name: String, addr: String, note: String, lat: Double, lng: Double): Long {
        val values = ContentValues()
        values.put("名稱", name)
        values.put("地址", addr)
        values.put("備註", note)
        values.put("緯度", lat)
        values.put("經度", lng)
        values.put("cos緯度", cos(lat*PI/180))
        values.put("sin緯度", sin(lat*PI/180))
        values.put("cos經度", cos(lng*PI/180))
        values.put("sin經度", sin(lng*PI/180))

        return writableDatabase.insert(tableName3, null, values)
    }
}