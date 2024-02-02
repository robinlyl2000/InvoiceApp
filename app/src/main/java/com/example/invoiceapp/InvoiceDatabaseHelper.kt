package com.example.invoiceapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

public class InvoiceDatabaseHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object{
        private const val DATABASE_NAME = "invoiceapp.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "allInvoices"
        private const val COLUMN_ID = "id"
        private const val COLUMN_INVOICENUMBER = "invoiceNumber"
        private const val COLUMN_YEAR = "year"
        private const val COLUMN_MONTH = "month"
        private const val COLUMN_DAY = "day"
        private const val COLUMN_STORENAME = "storeName"
        private const val COLUMN_AMOUNT = "amount"
        private const val COLUMN_DETAILS = "details"

    }

    // 新增 Table
    override fun onCreate(p0: SQLiteDatabase?) {
        val sql = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_INVOICENUMBER TEXT NOT NULL,
                $COLUMN_YEAR INTEGER NOT NULL,
                $COLUMN_MONTH INTGER NOT NULL,
                $COLUMN_DAY INTEGER NOT NULL,
                $COLUMN_STORENAME TEXT,
                $COLUMN_AMOUNT INTEGER,
                $COLUMN_DETAILS TEXT
            )
        """.trimIndent()

        p0?.execSQL(sql)
    }

    // 更新 Table
    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        val sql = "DROP TABLE IF EXISTS $TABLE_NAME"
        p0?.execSQL(sql)
        onCreate(p0)
    }

    // 插入新發票資料
    fun insertInvoice(invoice: Invoice){
        val values = ContentValues().apply {
            put(COLUMN_INVOICENUMBER, invoice.invoiceNumber)
            put(COLUMN_YEAR, invoice.year)
            put(COLUMN_MONTH, invoice.month)
            put(COLUMN_DAY, invoice.day)
            put(COLUMN_STORENAME, invoice.storeName)
            put(COLUMN_AMOUNT, invoice.amount)
            put(COLUMN_DETAILS, invoice.details)
        }
        writableDatabase.insert(TABLE_NAME, null, values)
        writableDatabase.close()
    }

    // 從 cursor 抓取對應的發票資料
    fun getInvoiceFromCursor(cursor: Cursor) : Invoice{
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
        val invoiceNumber = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INVOICENUMBER))
        val year = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_YEAR))
        val month = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MONTH))
        val day = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DAY))
        val storeName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STORENAME))
        val amount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT))
        val details = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DETAILS))

        return Invoice(id, invoiceNumber, year, month, day, storeName, amount, details)
    }

    // 藉由年分與月份抓取對應的資料
    fun getInvoicesByYearAndMonth(searchYear: Int, searchMonth: Int): List<Invoice> {
        val invoicesList = mutableListOf<Invoice>()
        val db = readableDatabase
        val query = """
            SELECT * FROM $TABLE_NAME 
            WHERE $COLUMN_YEAR = $searchYear AND $COLUMN_MONTH = $searchMonth
            ORDER BY $COLUMN_YEAR DESC, $COLUMN_MONTH DESC, $COLUMN_DAY DESC
        """.trimIndent()
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()){
            invoicesList.add(getInvoiceFromCursor(cursor))
        }
        cursor.close()
        db.close()

        return invoicesList
    }

    // 藉由年分、月份與日期抓取對應的資料
    fun getInvoicesByYearAndMonthAndDay(searchYear: Int, searchMonth: Int, searchDay: Int): List<Invoice> {
        val invoicesList = mutableListOf<Invoice>()
        val db = readableDatabase
        val query = """
            SELECT * FROM $TABLE_NAME 
            WHERE $COLUMN_YEAR = $searchYear AND $COLUMN_MONTH = $searchMonth AND $COLUMN_DAY = $searchDay
            ORDER BY $COLUMN_YEAR DESC, $COLUMN_MONTH DESC, $COLUMN_DAY DESC
        """.trimIndent()
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()){
            invoicesList.add(getInvoiceFromCursor(cursor))
        }
        cursor.close()
        db.close()

        return invoicesList
    }

    // 依照 ID 更新發票資料
    fun updateInvoice(invoice: Invoice){
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_INVOICENUMBER, invoice.invoiceNumber)
            put(COLUMN_YEAR, invoice.year)
            put(COLUMN_MONTH, invoice.month)
            put(COLUMN_DAY, invoice.day)
            put(COLUMN_STORENAME, invoice.storeName)
            put(COLUMN_AMOUNT, invoice.amount)
            put(COLUMN_DETAILS, invoice.details)
        }
        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(invoice.id.toString())
        db.update(TABLE_NAME, values, whereClause, whereArgs)
        db.close()
    }

    // 依照 ID 刪除發票資料
    fun deleteInvoice(invoiceId: Int){
        val db = writableDatabase
        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(invoiceId.toString())
        db.delete(TABLE_NAME, whereClause, whereArgs)
        db.close()
    }

    // 依照 ID 尋找對應的發票資料
    fun getInvoiceByID(invoiceId: Int): Invoice{
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = $invoiceId"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()

        val invoice = getInvoiceFromCursor(cursor)

        cursor.close()
        db.close()

        return invoice
    }

    // 抓出符合中獎資格的發票資料
    fun searchInvoiceByWinningNum(searchYear: Int, interval: String?, code: String) : MutableList<Pair<Invoice, String>>{

        val searchMonth : Pair<Int, Int>? = splitInterval(interval!!)

        val invoicesList: MutableList<Pair<Invoice, String>> = mutableListOf()
        val db = readableDatabase
        val query = """
            SELECT *,
                CASE
                    WHEN SUBSTRING($COLUMN_INVOICENUMBER, 4) = "$code" THEN 200000
                    WHEN SUBSTRING($COLUMN_INVOICENUMBER, 5) = SUBSTRING("$code", 2) THEN "40,000"
                    WHEN SUBSTRING($COLUMN_INVOICENUMBER, 6) = SUBSTRING("$code", 3) THEN "10,000"
                    WHEN SUBSTRING($COLUMN_INVOICENUMBER, 7) = SUBSTRING("$code", 4) THEN "4,000"
                    WHEN SUBSTRING($COLUMN_INVOICENUMBER, 8) = SUBSTRING("$code", 5) THEN "1,000"
                    WHEN SUBSTRING($COLUMN_INVOICENUMBER, 9) = SUBSTRING("$code", 6) THEN "200"
                    ELSE "0"
                END AS prize_amount
            FROM $TABLE_NAME
            WHERE
                $COLUMN_YEAR = $searchYear AND ($COLUMN_MONTH = ${searchMonth?.first} OR $COLUMN_MONTH = ${searchMonth?.second})
            ORDER BY $COLUMN_YEAR DESC, $COLUMN_MONTH DESC, $COLUMN_DAY DESC
        """.trimIndent() // 依照不同的條件，將對應的獎金寫入 prize_amount 欄位中

        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()){

            val prize = cursor.getString(cursor.getColumnIndexOrThrow("prize_amount"))
            if (prize == "0"){ // 若沒中獎，則不記錄
                continue
            }

            invoicesList.add(Pair(getInvoiceFromCursor(cursor), prize))
        }
        cursor.close()
        db.close()
        return invoicesList
    }

    // 將 "1 月 ~ 2 月" 轉換成 Pair(1, 2)
    fun splitInterval(inputString: String) : Pair<Int, Int>?{

        val regex = Regex("""(\d+) 月 ~ (\d+) 月""")

        val matchResult = regex.find(inputString)
        if (matchResult != null){
            val (firstNumber, secondNumber) = matchResult.destructured
            return Pair(firstNumber.toInt(), secondNumber.toInt())
        }
        return null
    }
}