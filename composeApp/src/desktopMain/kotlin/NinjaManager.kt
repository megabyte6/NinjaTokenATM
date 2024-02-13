import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileInputStream


class NinjaManager(val settings: Settings) {
    // Cache of previously searched Ninjas.
    // Holds a Map of the ninja's RFID to the corresponding row.
    private val cache = mutableMapOf<String, Int>()

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
            ?.also { cache[rfid] = it.rowNum }

    private fun findCachedRowByRFID(rfid: String) =
        cache[rfid]?.let { ninjaTokenSheet.getRow(it) } ?: findRowByRFID(rfid)

    fun findName(rfid: String) =
        findCachedRowByRFID(rfid)?.stringContentsOfCell(ninjaNameColumn) ?: "Hmm... I don't know this one"

    fun findBalance(rfid: String) =
        findCachedRowByRFID(rfid)?.stringContentsOfCell(ninjaBalanceColumn)?.toDoubleOrNull()?.toInt() ?: 0
}