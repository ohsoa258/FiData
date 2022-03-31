package com.fisk.dataaccess.utils.ftp;

import com.csvreader.CsvReader;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.ftp.ExcelDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.IntStream;

import static com.fisk.common.core.constants.ExcelConstants.EXCEL2003_SUFFIX_NAME;

/**
 * @author Lock
 * @version 1.0
 * @description
 * @date 2021/12/28 9:59
 */
@Slf4j
public class ExcelUtils {

    /**
     * @description 读取Excel文件
     * @author Lock
     * @date 2021/12/28 9:59
     * @version v1.0
     * @params filePath
     * @return org.apache.poi.ss.usermodel.Workbook
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
            log.error("读取Excel文件失败，【read】方法报错：", e);
        }
        return null;
    }

    /**
     * @description 从流中读取，上传文件可以直接获取文件流，无需暂存到服务器上
     * @author Lock
     * @date 2021/12/28 10:15
     * @version v1.0
     * @params inputStream 文件输入流
     * @params ext 文件后缀名
     * @return org.apache.poi.ss.usermodel.Workbook excel工作簿对象
     */
    private static Workbook readFromInputStream(InputStream inputStream, String ext) {
        try {
            if (EXCEL2003_SUFFIX_NAME.equals(ext)) {
                // Excel 2003
                return new HSSFWorkbook(inputStream);
            } else {
                // Excel 2007
                return new XSSFWorkbook(inputStream);
            }
        } catch (IOException e) {
            log.error("从流中读取excel文件失败，【readFromInputStream】方法报错，", e);
        }
        return null;
    }

    /**
     * @description 读取Excel内容，返回list，每一行存放一个list
     * @author Lock
     * @date 2021/12/28 10:19
     * @version v1.0
     * @params wb 工作簿对象
     * @params index sheet页
     * @return java.util.List<java.util.List < java.lang.String>>
     */
    private static List<List<String>> readExcelContentList(Workbook wb, int index) {
        if (wb != null) {
            List<List<String>> content = new ArrayList<>();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Sheet sheet = wb.getSheetAt(index);
            // excel行对象，0：第一行对象
            Row row = sheet.getRow(0);
            // 获取sheet页所有行数
            int rowNum = sheet.getLastRowNum();
            // 获取当前行的列数
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
     * @description 读取csv内容，返回list，每一行存放一个list
     * @author Lock
     * @date 2022/1/5 11:18
     * @version v1.0
     * @params wb
     * @params index
     * @return java.util.List<java.util.List < java.lang.String>>
     */
    private static List<List<String>> readCsvContentList(InputStream inputStream) {
        // 默认只查询十行
        List<List<String>> content = new ArrayList<>();
        CsvReader csvReader = new CsvReader(inputStream, Charset.forName("GBK"));
        try {
            while (csvReader.readRecord()) {
                content.add(Arrays.asList(csvReader.getValues()));
                if (content.size() >= 10) {
                    return content;
                }
            }
        } catch (Exception e) {
            throw new FkException(ResultEnum.READ_CSV_CONTENT_ERROR);
        }
        return content;
    }


    /**
     * @description 根据Cell类型设置数据
     * @author Lock
     * @date 2021/12/28 10:25
     * @version v1.0
     * @params cell excel单元格对象
     * @return java.lang.Object
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
                // 如果当前Cell的Type为STRING
                case Cell.CELL_TYPE_STRING:
                    cellvalue = cell.getRichStringCellValue().getString();
                    break;
                default:
                    break;
            }
        }
        return cellvalue;
    }

    /**
     * @description 读取Excel
     * @author Lock
     * @date 2021/12/28 10:28
     * @version v1.0
     * @params filePath Excel文件路径
     * @return java.util.List<java.util.List < java.lang.String>>
     */
    public static List<List<String>> readExcel(String filePath) {
        Workbook wb = read(filePath);
        // 默认获取第一个sheet页
        return readExcelContentList(wb, 0);
    }

    /**
     * @description 读取excel内容
     * @author Lock
     * @date 2021/12/28 10:29
     * @version v1.0
     * @params inputStream Excel文件流
     * @params ext 文件后缀名
     * @return java.util.List<com.fisk.dataaccess.dto.ftp.ExcelDTO>
     */
    public static List<ExcelDTO> readExcelFromInputStream(InputStream inputStream, String ext) {
        List<ExcelDTO> listDto = null;
        try {
            Workbook workbook = readFromInputStream(inputStream, ext);
            if (workbook == null) {
                return null;
            }
            // 获取sheet页数量
            int numberOfSheets = workbook.getNumberOfSheets();
            listDto = new ArrayList<>();

            List<ExcelDTO> finalListDto = listDto;
            IntStream.range(0, numberOfSheets).forEachOrdered(i -> {
                // 读取Excel内容，返回list，每一行存放一个list
                List<List<String>> lists = readExcelContentList(workbook, i);
                ExcelDTO excelDTO = new ExcelDTO();
                // excel预览内容
                excelDTO.excelContent = lists;
                // excel字段列表
                excelDTO.excelField = lists.get(0);
                // sheet名称
                excelDTO.sheetName = workbook.getSheetName(i);
                finalListDto.add(excelDTO);
            });
        } catch (Exception e) {
            throw new FkException(ResultEnum.READ_EXCEL_CONTENT_ERROR);
        }
        return listDto;
    }

    /**
     * @return java.util.List<com.fisk.dataaccess.dto.ftp.ExcelDTO>
     * @description 读取csv文件
     * @author Lock
     * @date 2022/1/5 11:15
     * @version v1.0
     * @params inputStream
     * @params ext
     */
    public static List<ExcelDTO> readCsvFromInputStream(InputStream inputStream, String filename) {
        List<ExcelDTO> listDto = null;
        try {
            listDto = new ArrayList<>();
            // 读取csv内容，返回list，每一行存放一个list
            List<List<String>> lists = readCsvContentList(inputStream);
            ExcelDTO excelDTO = new ExcelDTO();
            // csv内容
            excelDTO.excelContent = lists;
            // csv没有多sheet页,本方法中莫瑞诺指定文件名为sheet名
            excelDTO.sheetName = filename;
            // 字段列表
            excelDTO.excelField = lists.get(0);
            listDto.add(excelDTO);
        } catch (Exception e) {
            throw new FkException(ResultEnum.READ_CSV_CONTENT_ERROR);
        }
        return listDto;
    }
}

