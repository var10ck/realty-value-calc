package helpers

import dao.entities.realty.RealtyObject
import dto.realty.RealtyObjectExcelDTO
import exceptions._
import org.apache.poi.ss.usermodel.{BorderStyle, CellType, FillPatternType, IndexedColors}
import org.apache.poi.xssf.usermodel.{XSSFCell, XSSFWorkbook}
import zio.ZIO

import java.io.{FileOutputStream, InputStream}
import java.time.format.{DateTimeFormatter, FormatStyle}
import scala.util.Try

object ExcelHelper {

    /** Transform xlsx-file with records about realty objects into List of RealtyObjects
      * @param in
      *   input stream containing binary xlsx encoded data
      */
    def transformXlsxToObject(in: InputStream): ZIO[Any, Throwable, List[RealtyObjectExcelDTO]] =
        (for {
            workbook <- ZIO.from(new XSSFWorkbook(in))
            sheet <- ZIO.from(workbook.getSheetAt(0))
            lastNum = sheet.getLastRowNum
            data <- ZIO.foreachPar((1 to lastNum).toList) { rowNum =>
                for {
                    row <- ZIO.from(sheet.getRow(rowNum))
                    location <- ZIO.from(row.getCell(0))
                    _ <- ZIO.from(location.setCellType(CellType.STRING))

                    roomsNumber <- ZIO.from(row.getCell(1))
                    _ <- ZIO.from(roomsNumber.setCellType(CellType.STRING))
                    segment <- ZIO.from(row.getCell(2))
                    _ <- ZIO.from(segment.setCellType(CellType.STRING))
                    floorCount <- ZIO.from(row.getCell(3))
                    _ <- ZIO.from(floorCount.setCellType(CellType.STRING))
                    wallMaterial <- ZIO.from(row.getCell(4))
                    _ <- ZIO.from(wallMaterial.setCellType(CellType.STRING))
                    floorNumber <- ZIO.from(row.getCell(5))
                    _ <- ZIO.from(floorNumber.setCellType(CellType.STRING))
                    totalArea <- ZIO.from(row.getCell(6))
                    _ <- ZIO.from(totalArea.setCellType(CellType.STRING))
                    kitchenArea <- ZIO.from(row.getCell(7))
                    _ <- ZIO.from(kitchenArea.setCellType(CellType.STRING))
                    gotBalcony <- ZIO.from(row.getCell(8))
                    _ <- ZIO.from(gotBalcony.setCellType(CellType.STRING))
                    distanceFromMetro <- ZIO.from(row.getCell(9))
                    _ <- ZIO.from(distanceFromMetro.setCellType(CellType.STRING))
                    condition <- ZIO.from(row.getCell(10))
                    _ <- ZIO.from(condition.setCellType(CellType.STRING))
                    // "студия" и другие данные будут считаться как 1 комната
                    roomsNumberTransformed <- ZIO
                        .whenCase(roomsNumber.getStringCellValue.toLowerCase) {
                            case num if Try(num.toInt).isSuccess => ZIO.succeed(num.toInt)
                            case _ => ZIO.succeed(1)
                        }.map(_.get)
                    gotBalconyBool <- ZIO
                        .whenCase(gotBalcony.getStringCellValue.toLowerCase) {
                            case "да" => ZIO.succeed(true)
                            case "нет" => ZIO.succeed(false)
                            case invalidData =>
                                ZIO.fail(new Exception(s"Invalid data in cell (${rowNum + 1}, 8): $invalidData"))
                        }
                        .map(_.get)
                } yield RealtyObjectExcelDTO(
                  location.getStringCellValue,
                    roomsNumberTransformed,
                  segment.getStringCellValue,
                  floorCount.getStringCellValue.toInt,
                  wallMaterial.getStringCellValue,
                  floorNumber.getStringCellValue.toInt,
                  totalArea.getStringCellValue.replace(",", ".").toDouble,
                  kitchenArea.getStringCellValue.replace(",", ".").toDouble,
                  gotBalconyBool,
                  condition.getStringCellValue,
                  distanceFromMetro.getStringCellValue.toInt
                )
            }
        } yield data).mapError(e => ExcelParsingException(e.getMessage))

    /** Writes RealtyObjects in file within xlsx format
      * @param fos
      *   FileOutputStream to write in
      * @param objects
      *   objects to write
      */
    def transformObjectsToXlsx(
        fos: FileOutputStream,
        objects: List[RealtyObject]): ZIO[Any, ExcelWritingException, Unit] = {
        val widthInChar: Int => Int = _ * 256
        (for {
            // Create workbook
            workbook <- ZIO.from(new XSSFWorkbook())
            sheet <- ZIO.from(workbook.createSheet("RealtyObjects"))
            _ = sheet.setColumnWidth(0, widthInChar(40))
            _ = sheet.setColumnWidth(1, widthInChar(10))
            _ = sheet.setColumnWidth(2, widthInChar(18))
            _ = sheet.setColumnWidth(3, widthInChar(9))
            _ = sheet.setColumnWidth(4, widthInChar(14))
            _ = sheet.setColumnWidth(5, widthInChar(17))
            _ = sheet.setColumnWidth(6, widthInChar(12))
            _ = sheet.setColumnWidth(7, widthInChar(12))
            _ = sheet.setColumnWidth(8, widthInChar(20))
            _ = sheet.setColumnWidth(9, widthInChar(15))
            _ = sheet.setColumnWidth(10, widthInChar(14))
            _ = sheet.setColumnWidth(11, widthInChar(20))
            _ = sheet.setColumnWidth(12, widthInChar(24))
            _ = sheet.setColumnWidth(13, widthInChar(24))

            // Create header style
            headerStyle = workbook.createCellStyle
            _ = headerStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex)
            _ = headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)
            _ = headerStyle.setWrapText(true)

            headerFont = workbook.createFont
            _ = headerFont.setFontName("Arial")
            _ = headerFont.setFontHeightInPoints(12.toShort)
            _ = headerFont.setBold(true)
            _ = headerStyle.setFont(headerFont)

            _ = headerStyle.setBorderTop(BorderStyle.THICK)
            _ = headerStyle.setBorderLeft(BorderStyle.THICK)
            _ = headerStyle.setBorderRight(BorderStyle.THICK)
            _ = headerStyle.setBorderBottom(BorderStyle.THICK)

            // Create cell style
            dataCellStyle = workbook.createCellStyle
            _ = dataCellStyle.setWrapText(true)
            _ = dataCellStyle.setBorderTop(BorderStyle.THIN)
            _ = dataCellStyle.setBorderLeft(BorderStyle.THIN)
            _ = dataCellStyle.setBorderRight(BorderStyle.THIN)
            _ = dataCellStyle.setBorderBottom(BorderStyle.THIN)

            cellFont = workbook.createFont
            _ = cellFont.setFontName("Arial")
            _ = cellFont.setFontHeightInPoints(12.toShort)
            _ = dataCellStyle.setFont(cellFont)

            // Create header and set it's style
            headerRow = sheet.createRow(0)

            locationCell = headerRow.createCell(0)
            _ = locationCell.setCellValue("Местоположение")
            roomsCountCell = headerRow.createCell(1)
            _ = roomsCountCell.setCellValue("Количество комнат")
            segmentCell = headerRow.createCell(2)
            _ = segmentCell.setCellValue("Сегмент")
            floorCount = headerRow.createCell(3)
            _ = floorCount.setCellValue("Этажность дома")
            wallMaterialCell = headerRow.createCell(4)
            _ = wallMaterialCell.setCellValue("Материал стен")
            floorNumberCell = headerRow.createCell(5)
            _ = floorNumberCell.setCellValue("Этаж расположения")
            totalAreaCell = headerRow.createCell(6)
            _ = totalAreaCell.setCellValue("Площадь квартиры, кв.м")
            kitchenAreaCell = headerRow.createCell(7)
            _ = kitchenAreaCell.setCellValue("Площадь кухни, кв.м")
            gotBalconyCell = headerRow.createCell(8)
            _ = gotBalconyCell.setCellValue("Наличие балкона/лоджии")
            conditionCell = headerRow.createCell(9)
            _ = conditionCell.setCellValue("Удаленность от станции метро")
            distanceFromMetroCell = headerRow.createCell(10)
            _ = distanceFromMetroCell.setCellValue("Состояние")
            calculatedValueCell = headerRow.createCell(11)
            _ = calculatedValueCell.setCellValue("Рассчитанная рыночная стоимость")
            createdAtCell = headerRow.createCell(12)
            _ = createdAtCell.setCellValue("Добавлено на портал")
            updatedAtCell = headerRow.createCell(13)
            _ = updatedAtCell.setCellValue("Время последнего обновления")
            _ = List(
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

            dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            result <- ZIO.foreachPar(objects.filter(_.calculatedValue.isDefined).zipWithIndex) {
                case (realtyObject: RealtyObject, index: Int) =>
                    ZIO.from {
                        val row = sheet.createRow(index + 1)
                        chainCell(row.createCell(0))(_.setCellValue(realtyObject.location)).setCellStyle(dataCellStyle)
                        chainCell(row.createCell(1))(_.setCellValue(realtyObject.roomsNumber))
                            .setCellStyle(dataCellStyle)
                        chainCell(row.createCell(2))(_.setCellValue(realtyObject.segment)).setCellStyle(dataCellStyle)
                        chainCell(row.createCell(3))(_.setCellValue(realtyObject.floorCount))
                            .setCellStyle(dataCellStyle)
                        chainCell(row.createCell(4))(_.setCellValue(realtyObject.wallMaterial))
                            .setCellStyle(dataCellStyle)
                        chainCell(row.createCell(5))(_.setCellValue(realtyObject.floorNumber))
                            .setCellStyle(dataCellStyle)
                        chainCell(row.createCell(6))(_.setCellValue(realtyObject.totalArea)).setCellStyle(dataCellStyle)
                        chainCell(row.createCell(7))(_.setCellValue(realtyObject.kitchenArea))
                            .setCellStyle(dataCellStyle)
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
            }
            _ <- ZIO.from(workbook.write(fos))
        } yield ()).mapError { e =>
            e.printStackTrace()
            ExcelWritingException(e.getMessage)
        }
    }

    private def chainCell[A](cell: XSSFCell)(f: XSSFCell => A): XSSFCell = {
        f(cell)
        cell
    }
}
