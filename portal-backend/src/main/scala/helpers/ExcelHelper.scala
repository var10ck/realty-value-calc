package helpers

import dao.entities.realty.RealtyObject
import dto.realty.RealtyObjectExcelDTO
import exceptions._
import org.apache.poi.ss.usermodel.{BorderStyle, CellStyle, CellType, FillPatternType, IndexedColors}
import org.apache.poi.xssf.usermodel.{XSSFCell, XSSFFactory, XSSFWorkbook}
import zio.ZIO

import java.io.{FileInputStream, FileOutputStream, InputStream}
import java.time.format.{DateTimeFormatter, FormatStyle}
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFWorkbook

object ExcelHelper {

    /** Transform xlsx-file with records about realty objects into List of RealtyObjects
      * @param in
      *   input stream containing binary xlsx encoded data
      */
    def transformXlsxToObject(in: InputStream): ZIO[Any, Throwable, List[RealtyObjectExcelDTO]] =
        ZIO.from {
            val workbook = new XSSFWorkbook(in)
            val sheet = workbook.getSheetAt(0)
            val lastNum = sheet.getLastRowNum
            val data = (1 to lastNum).map { rowNum =>
                val row = sheet.getRow(rowNum)
                // местоположение
                val location = row.getCell(0)
                location.setCellType(CellType.STRING)

                // Количество комнат
                val roomsNumber = row.getCell(1)
                roomsNumber.setCellType(CellType.STRING)

                // Сегмент
                val segment = row.getCell(2)
                segment.setCellType(CellType.STRING)

                // Количество этажей
                val floorCount = row.getCell(3)
                floorCount.setCellType(CellType.STRING)

                // Материал стен
                val wallMaterial = row.getCell(4)
                wallMaterial.setCellType(CellType.STRING)

                // Этаж расположения
                val floorNumber = row.getCell(5)
                floorNumber.setCellType(CellType.STRING)

                // Площадь квартиры
                val totalArea = row.getCell(6)
                totalArea.setCellType(CellType.STRING)

                // Площаль кухни
                val kitchenArea = row.getCell(7)
                kitchenArea.setCellType(CellType.STRING)

                // Наличие балкона/лоджии
                val gotBalcony = row.getCell(8)
                gotBalcony.setCellType(CellType.STRING)

                // Состояние квартиры
                val condition = row.getCell(9)
                condition.setCellType(CellType.STRING)

                // Удаленность от метро
                val distanceFromMetro = row.getCell(10)
                distanceFromMetro.setCellType(CellType.STRING)

                val gotBalconyBool: Boolean = gotBalcony.getStringCellValue.toLowerCase match {
                    case "есть" => true
                    case "нет" => false
                    case _ => throw new Exception(s"Invalid data in cell (${rowNum + 1}, 8)")
                }
                RealtyObjectExcelDTO(
                  location.getStringCellValue,
                  roomsNumber.getStringCellValue.toInt,
                  segment.getStringCellValue,
                  floorCount.getStringCellValue.toInt,
                  wallMaterial.getStringCellValue,
                  floorNumber.getStringCellValue.toInt,
                  totalArea.getStringCellValue.toDouble,
                  kitchenArea.getStringCellValue.toDouble,
                  gotBalconyBool,
                  condition.getStringCellValue,
                  distanceFromMetro.getStringCellValue.toInt
                )
            }
            in.close()
            data.toList
        }.mapError(e => ExcelParsingException(e.getMessage))

    /** Writes RealtyObjects in file within xlsx format
      * @param fos
      *   FileOutputStream to write in
      * @param objects
      *   objects to write
      */
    def transformObjectsToXlsx(
        fos: FileOutputStream,
        objects: List[RealtyObject]): ZIO[Any, ExcelWritingException, Unit] = ZIO.from {
        // Create workbook
        val workbook = new XSSFWorkbook()
        val sheet = workbook.createSheet("RealtyObjects")
        val widthInChar: Int => Int = _ * 256
        sheet.setColumnWidth(0, widthInChar(40))
        sheet.setColumnWidth(1, widthInChar(10))
        sheet.setColumnWidth(2, widthInChar(18))
        sheet.setColumnWidth(3, widthInChar(9))
        sheet.setColumnWidth(4, widthInChar(14))
        sheet.setColumnWidth(5, widthInChar(17))
        sheet.setColumnWidth(6, widthInChar(12))
        sheet.setColumnWidth(7, widthInChar(12))
        sheet.setColumnWidth(8, widthInChar(20))
        sheet.setColumnWidth(9, widthInChar(15))
        sheet.setColumnWidth(10, widthInChar(14))
        sheet.setColumnWidth(11, widthInChar(20))
        sheet.setColumnWidth(12, widthInChar(24))
        sheet.setColumnWidth(13, widthInChar(24))

        // Create header style
        val headerStyle = workbook.createCellStyle
        headerStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex)
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)
        headerStyle.setWrapText(true)

        val headerFont = workbook.createFont
        headerFont.setFontName("Arial")
        headerFont.setFontHeightInPoints(12.toShort)
        headerFont.setBold(true)
        headerStyle.setFont(headerFont)

        headerStyle.setBorderTop(BorderStyle.THICK)
        headerStyle.setBorderLeft(BorderStyle.THICK)
        headerStyle.setBorderRight(BorderStyle.THICK)
        headerStyle.setBorderBottom(BorderStyle.THICK)

        // Create cell style
        val dataCellStyle = workbook.createCellStyle
        dataCellStyle.setWrapText(true)
        dataCellStyle.setBorderTop(BorderStyle.THIN)
        dataCellStyle.setBorderLeft(BorderStyle.THIN)
        dataCellStyle.setBorderRight(BorderStyle.THIN)
        dataCellStyle.setBorderBottom(BorderStyle.THIN)

        val cellFont = workbook.createFont
        cellFont.setFontName("Arial")
        cellFont.setFontHeightInPoints(12.toShort)
        dataCellStyle.setFont(cellFont)

        // Create header and set it's style
        val headerRow = sheet.createRow(0)

        val locationCell = headerRow.createCell(0)
        locationCell.setCellValue("Местоположение")
        val roomsCountCell = headerRow.createCell(1)
        roomsCountCell.setCellValue("Количество комнат")
        val segmentCell = headerRow.createCell(2)
        segmentCell.setCellValue("Сегмент")
        val floorCount = headerRow.createCell(3)
        floorCount.setCellValue("Этажность дома")
        val wallMaterialCell = headerRow.createCell(4)
        wallMaterialCell.setCellValue("Материал стен")
        val floorNumberCell = headerRow.createCell(5)
        floorNumberCell.setCellValue("Этаж расположения")
        val totalAreaCell = headerRow.createCell(6)
        totalAreaCell.setCellValue("Площадь квартиры, кв.м")
        val kitchenAreaCell = headerRow.createCell(7)
        kitchenAreaCell.setCellValue("Площадь кухни, кв.м")
        val gotBalconyCell = headerRow.createCell(8)
        gotBalconyCell.setCellValue("Наличие балкона/лоджии")
        val conditionCell = headerRow.createCell(9)
        conditionCell.setCellValue("Удаленность от станции метро")
        val distanceFromMetroCell = headerRow.createCell(10)
        distanceFromMetroCell.setCellValue("Состояние")
        val calculatedValueCell = headerRow.createCell(11)
        calculatedValueCell.setCellValue("Рассчитанная рыночная стоимость")
        val createdAtCell = headerRow.createCell(12)
        createdAtCell.setCellValue("Добавлено на портал")
        val updatedAtCell = headerRow.createCell(13)
        updatedAtCell.setCellValue("Время последнего обновления")

        List(
          locationCell,
          roomsCountCell,
          segmentCell,
          floorCount,
          wallMaterialCell,
          floorNumberCell,
          totalAreaCell,
          kitchenAreaCell,
          gotBalconyCell,
          conditionCell,
          distanceFromMetroCell,
          calculatedValueCell,
          createdAtCell,
          updatedAtCell
        ).foreach(_.setCellStyle(headerStyle))

        val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)

        objects.filter(_.calculatedValue.isDefined).zipWithIndex.foreach {
            case (realtyObject: RealtyObject, index: Int) =>
                val row = sheet.createRow(index + 1)
                chainCell(row.createCell(0))(_.setCellValue(realtyObject.location)).setCellStyle(dataCellStyle)
                chainCell(row.createCell(1))(_.setCellValue(realtyObject.roomsNumber)).setCellStyle(dataCellStyle)
                chainCell(row.createCell(2))(_.setCellValue(realtyObject.segment)).setCellStyle(dataCellStyle)
                chainCell(row.createCell(3))(_.setCellValue(realtyObject.floorCount)).setCellStyle(dataCellStyle)
                chainCell(row.createCell(4))(_.setCellValue(realtyObject.wallMaterial)).setCellStyle(dataCellStyle)
                chainCell(row.createCell(5))(_.setCellValue(realtyObject.floorNumber)).setCellStyle(dataCellStyle)
                chainCell(row.createCell(6))(_.setCellValue(realtyObject.totalArea)).setCellStyle(dataCellStyle)
                chainCell(row.createCell(7))(_.setCellValue(realtyObject.kitchenArea)).setCellStyle(dataCellStyle)
                chainCell(row.createCell(8))(_.setCellValue(if (realtyObject.gotBalcony) "есть" else "нет"))
                    .setCellStyle(dataCellStyle)
                chainCell(row.createCell(9))(_.setCellValue(realtyObject.condition)).setCellStyle(dataCellStyle)
                chainCell(row.createCell(10))(_.setCellValue(realtyObject.distanceFromMetro))
                    .setCellStyle(dataCellStyle)
                chainCell(row.createCell(11))(_.setCellValue(realtyObject.calculatedValue.getOrElse(0L)))
                    .setCellStyle(dataCellStyle)
                chainCell(row.createCell(12))(_.setCellValue(realtyObject.createdAt.format(dateTimeFormatter)))
                    .setCellStyle(dataCellStyle)
                chainCell(row.createCell(13))(_.setCellValue(realtyObject.updatedAt.format(dateTimeFormatter)))
                    .setCellStyle(dataCellStyle)
        }
        workbook.write(fos)
    }.mapError { e =>
        e.printStackTrace()
        ExcelWritingException(e.getMessage)
    }

    private def chainCell[A](cell: XSSFCell)(f: XSSFCell => A): XSSFCell = {
        f(cell)
        cell
    }
}
