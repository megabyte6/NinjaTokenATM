package com.github.megabyte6.ninjatokenatm

import kotlinx.serialization.Serializable
import org.apache.poi.EmptyFileException
import org.apache.poi.EncryptedDocumentException
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException


@Serializable
data class Settings(
    var ninjaTokenWorkbookPath: String = "",
    var sheet: String = "",
    var rfidColumn: String = "",
    var ninjaNameColumn: String = "",
    var ninjaBalanceColumn: String = "",
    var rfidLength: Int = 0,
    var rfidScannerHitsEnter: Boolean = true,
    var darkTheme: Boolean = true
) {
    companion object {
        val EMPTY = Settings()
    }

    /**
     * @throws FileNotFoundException if the ninjaTokenWorkbookPath passed does not exist nor points to a file
     * @throws EncryptedDocumentException if the workbook is encrypted
     * @throws EmptyFileException if the workbook is empty
     * @throws SheetNotFoundException if the sheet name given does not exist
     * @throws SheetColumnNotFoundException if the column name given does not exist
     */
    fun validate() {
        val ninjaTokenWorkbookFile = File(ninjaTokenWorkbookPath)
        if (!ninjaTokenWorkbookFile.exists() || ninjaTokenWorkbookFile.isDirectory) {
            throw FileNotFoundException("The path '$ninjaTokenWorkbookPath' for 'ninjaTokenSheetPath' does not point to a file.")
        }

        val ninjaTokenWorkbook: Workbook? = WorkbookFactory.create(FileInputStream(ninjaTokenWorkbookFile))
        if (ninjaTokenWorkbook?.getSheet(sheet) == null) {
            throw SheetNotFoundException("A sheet named '$sheet' was not found.")
        }

        val ninjaTokenSheet: Sheet? = ninjaTokenWorkbook.getSheet(sheet)
        if (ninjaTokenSheet?.getRow(0)?.none { it?.stringCellValue == rfidColumn } == true) {
            throw SheetColumnNotFoundException("No column named '$rfidColumn' was found.")
        }
        if (ninjaTokenSheet?.getRow(0)?.none { it?.stringCellValue == ninjaNameColumn } == true) {
            throw SheetColumnNotFoundException("No column named '$ninjaNameColumn' was found.")
        }
        if (ninjaTokenSheet?.getRow(0)?.none { it?.stringCellValue == ninjaBalanceColumn } == true) {
            throw SheetColumnNotFoundException("No column named '$ninjaBalanceColumn' was found.")
        }
    }
}

class SheetNotFoundException(message: String) : Exception(message)

class SheetColumnNotFoundException(message: String) : Exception(message)