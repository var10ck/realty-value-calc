package helpers

import dto.realty.RealtyObjectExcelDTO

import java.io.{File, FileInputStream, InputStream}
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.{XSSFFactory, XSSFWorkbook}
import zio.ZIO

object ExcelHelper {

    def transformXlsToObject(in: InputStream): ZIO[Any, Throwable, List[RealtyObjectExcelDTO]] =
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

                RealtyObjectExcelDTO(
                  location.getStringCellValue,
                  roomsNumber.getStringCellValue.toInt,
                  segment.getStringCellValue,
                  floorCount.getStringCellValue.toInt,
                  wallMaterial.getStringCellValue,
                  floorNumber.getStringCellValue.toInt,
                  totalArea.getStringCellValue.toDouble,
                  kitchenArea.getStringCellValue.toDouble,
                  gotBalcony.getStringCellValue,
                  condition.getStringCellValue,
                  distanceFromMetro.getStringCellValue.toInt
                )
            }
            in.close()
            data.toList
        }.mapError(e => exceptions.ExcelParsingException(e.getMessage))

}
