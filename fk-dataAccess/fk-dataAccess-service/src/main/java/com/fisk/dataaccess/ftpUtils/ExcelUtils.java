package com.fisk.dataaccess.ftpUtils;

import com.fisk.dataaccess.dto.ftp.ExcelDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * @author : lock
 * @description: Excel parse util.
 * @date : 2021/12/27 10:45
 */
public class ExcelUtils {


    /**
     * logger
     */
    private final static Logger logger = LoggerFactory.getLogger(ExcelUtils.class);

    /**
     * 读取Excel文件
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    private static Workbook read(String filePath) {
        if (filePath == null) {
            return null;
        }
        String ext = filePath.substring(filePath.lastIndexOf("."));
        try {
            FileInputStream inputStream = new FileInputStream(filePath);
            return readFromInputStream(inputStream, ext);
        } catch (FileNotFoundException e) {
            logger.error("FileNotFoundException", e);
        }
        return null;
    }

    /**
     * 从流中读取，上传文件可以直接获取文件流，无需暂存到服务器上
     *
     * @param inputStream inputStream
     * @param ext 文件后缀名
     * @return excel工作簿对象
     */
    private static Workbook readFromInputStream(InputStream inputStream, String ext) {
        try {
            if (".xls".equals(ext)) {
                return new HSSFWorkbook(inputStream);
            } else {
                return new XSSFWorkbook(inputStream);
            }
        } catch (FileNotFoundException e) {
            logger.error("FileNotFoundException", e);
        } catch (IOException e) {
            logger.error("IOException", e);
        }
        return null;
    }

    /**
     * 读取Excel内容，返回list，每一行存放一个list
     *
     * @param wb
     * @return
     */
    private static List<List<String>> readExcelContentList(Workbook wb, int index) {
        if (wb != null) {
            List<List<String>> content = new ArrayList<>();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Sheet sheet = wb.getSheetAt(index);
//            Sheet sheet1 = wb.getSheet("tb_app_registration.xlsx");
//            int numberOfSheets = wb.getNumberOfSheets();
//            String sheetName = wb.getSheetName(index);
//            System.out.println("获取指定的sheet名称" + sheetName);
//            System.out.println("numberOfSheets = " + numberOfSheets);
            Row row = sheet.getRow(0);
            // 所有行数
            int rowNum = sheet.getLastRowNum();
            int colNum = row.getPhysicalNumberOfCells();
            // for循环 0: 从表头开始读取  1: 从正文开始读取
            // 正文内容应该从第二行开始, 第一行为表头的标题
            // 默认只读取前十条
            int tenLines = Math.min(rowNum, 10);
            for (int ri = 0; ri <= tenLines; ri++) {
                row = sheet.getRow(ri);
                int ci = 0;
                List<String> col = new ArrayList<>();
                while (ci < colNum) {
                    Object obj = getCellFormatValue(row.getCell(ci++));
                    obj = (obj instanceof Date) ? simpleDateFormat.format((Date) obj) : obj;
                    col.add((String) obj);
                }
                // 如果行是纯空白字符串，将被过滤
                // 但有空列，而不是全部空白，将不会破坏行信息，而不会被过滤。
                long count = col.stream().filter(StringUtils::isNoneBlank).count();
                Optional.of(col).filter(x -> count > 0).ifPresent(content::add);
            }
            return content;
        }
        return null;
    }


    /**
     * 根据Cell类型设置数据
     *
     * @param cell
     * @return
     */
    private static Object getCellFormatValue(Cell cell) {
        Object cellvalue = "";
        if (cell != null) {
            // 判断当前Cell的Type
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC:
                case Cell.CELL_TYPE_FORMULA:
                    // 判断当前的cell为Date, 取时间类型；数字则转字符串
                    cellvalue = DateUtil.isCellDateFormatted(cell) ? cell.getDateCellValue() : String.valueOf(cell.getNumericCellValue());
                    break;
                case Cell.CELL_TYPE_STRING:// 如果当前Cell的Type为STRING
                    cellvalue = cell.getRichStringCellValue().getString();
                    break;
                default:
                    break;
            }
        }
        return cellvalue;
    }

    /**
     * 读取Excel
     *
     * @param filePath Excel文件路径
     * @return
     */
    public static List<List<String>> readExcel(String filePath) {
        Workbook wb = read(filePath);
        return readExcelContentList(wb, 1);
    }

    /**
     * 读取Excel
     *
     * @param inputStream Excel文件流
     * @return excel对象
     */
    public static List<ExcelDTO> readExcelFromInputStream(InputStream inputStream, String ext) {
        Workbook workbook = readFromInputStream(inputStream, ext);
        if (workbook == null) {
            return null;
        }
        // 获取sheet页数量
        int numberOfSheets = workbook.getNumberOfSheets();
        List<ExcelDTO> listDto = new ArrayList<>();

        IntStream.range(0, numberOfSheets).forEachOrdered(i -> {
            List<List<String>> lists = readExcelContentList(workbook, i);
            ExcelDTO excelDTO = new ExcelDTO();
            // excel预览内容
            excelDTO.excelContent = lists;
            // excel字段列表
            excelDTO.excelField = lists.get(0);
            // sheet名称
            excelDTO.sheetName = workbook.getSheetName(i);
            listDto.add(excelDTO);
        });
        return listDto;
    }
}

