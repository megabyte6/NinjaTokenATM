package com.github.megabyte6.ninjatokenatm

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileInputStream


class NinjaManager(val settings: Settings) {
    // Cache of previously searched Ninjas.
    // Holds a Map of the ninja's RFID to the corresponding row.
    private val rfidCache = mutableMapOf<String, Int>()

    // Holds a Map of the ninja's lowercased name to the corresponding row.
    private val nameCache = mutableMapOf<String, Int>()

    private val ninjaTokenSheet: Sheet = WorkbookFactory.create(FileInputStream(settings.ninjaTokenWorkbookPath))!!
        .getSheet(settings.sheet)

    private val rfidColumn =
        ninjaTokenSheet.getRow(0).first { it?.stringCellValue == settings.rfidColumn }.columnIndex
    private val ninjaNameColumn =
        ninjaTokenSheet.getRow(0).first { it?.stringCellValue == settings.ninjaNameColumn }.columnIndex
    private val ninjaBalanceColumn =
        ninjaTokenSheet.getRow(0).first { it?.stringCellValue == settings.ninjaBalanceColumn }.columnIndex

    private fun Row.stringContentsOfCell(column: Int): String {
        val cell = getCell(column) ?: return ""
        return when (cell.cellType) {
            CellType.NUMERIC -> cell.numericCellValue.toBigDecimal().toPlainString()
            CellType.STRING -> cell.stringCellValue
            CellType.FORMULA -> when (cell.cachedFormulaResultType) {
                CellType.NUMERIC -> cell.numericCellValue.toBigDecimal().toPlainString()
                CellType.STRING -> cell.stringCellValue
                CellType.BOOLEAN -> cell.booleanCellValue.toString()
                else -> ""
            }

            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.ERROR -> cell.errorCellValue.toString()
            else -> ""
        }
    }

    private fun findRowByRFID(rfid: String) =
        ninjaTokenSheet.firstOrNull { it.stringContentsOfCell(rfidColumn) == rfid }
            ?.also {
                rfidCache[rfid] = it.rowNum
                nameCache[it.stringContentsOfCell(ninjaNameColumn).lowercase()] = it.rowNum
            }

    private fun findRowByName(name: String) =
        ninjaTokenSheet.firstOrNull { it.stringContentsOfCell(ninjaNameColumn).lowercase() == name.lowercase() }
            ?.also {
                nameCache[name.lowercase()] = it.rowNum
                rfidCache[it.stringContentsOfCell(rfidColumn)] = it.rowNum
            }

    private fun findCachedRowByRFID(rfid: String) =
        rfidCache[rfid]?.let { ninjaTokenSheet.getRow(it) } ?: findRowByRFID(rfid)

    private fun findCachedRowByName(name: String) =
        nameCache[name.lowercase()]?.let { ninjaTokenSheet.getRow(it) } ?: findRowByName(name)

    fun rfidExists(rfid: String) = findCachedRowByRFID(rfid) != null

    fun nameExists(name: String) = findCachedRowByName(name) != null

    fun findNameFromRFID(rfid: String) =
        findCachedRowByRFID(rfid)?.stringContentsOfCell(ninjaNameColumn) ?: Strings.INVALID_RFID_MESSAGE

    fun findRFIDFromName(name: String) =
        findCachedRowByName(name)?.stringContentsOfCell(rfidColumn) ?: Strings.INVALID_RFID_MESSAGE

    fun findBalanceByRFID(rfid: String) =
        findCachedRowByRFID(rfid)?.stringContentsOfCell(ninjaBalanceColumn)?.toDoubleOrNull()?.toInt() ?: 0

    fun findBalanceByName(name: String) =
        findCachedRowByName(name)?.stringContentsOfCell(ninjaBalanceColumn)?.toDoubleOrNull()?.toInt() ?: 0
}