package com.fisk.dataaccess.utils.ftp;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.csvreader.CsvReader;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.ftp.ExcelDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
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
     * @return org.apache.poi.ss.usermodel.Workbook
     * @description 读取Excel文件
     * @author Lock
     * @date 2021/12/28 9:59
     * @version v1.0
     * @params filePath
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
     * @return org.apache.poi.ss.usermodel.Workbook excel工作簿对象
     * @description 从流中读取，上传文件可以直接获取文件流，无需暂存到服务器上
     * @author Lock
     * @date 2021/12/28 10:15
     * @version v1.0
     * @params inputStream 文件输入流
     * @params ext 文件后缀名
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
        } catch (Exception e) {
            log.error("从流中读取excel文件失败，【readFromInputStream】方法报错，", e);
        }
        return null;
    }

    /**
     * @return java.util.List<java.util.List < java.lang.String>>
     * @description 读取Excel内容，返回list，每一行存放一个list
     * @author Lock
     * @date 2021/12/28 10:19
     * @version v1.0
     * @params wb 工作簿对象
     * @params index sheet页
     */
    private static List<List<Object>> readExcelContentList(Workbook wb, int index, int startRow) {
        if (wb != null) {
            try {
                List<List<Object>> content = new ArrayList<>();
                //2023-05-31 李世纪修改 日期格式改为yyyy-MM-dd HH:mm:ss 避免日期数据丢失
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //指定时区为东八区，避免时区偏移的现象出现
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                Sheet sheet = wb.getSheetAt(index);
                // 获取行数
                int getRow = 0;
                short lastCellNum = 130;
                //解决最大行数一直变的问题,拿第一次得到的行数
                int physicalNumberOfRows = sheet.getPhysicalNumberOfRows();
                for (int i = 0; i <= physicalNumberOfRows; i++) {
                    if (getRow < startRow) {
                        getRow++;
                        continue;
                    }
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        lastCellNum = row.getLastCellNum() > lastCellNum ? row.getLastCellNum() : lastCellNum;
                    } else {
                        row = sheet.createRow(i);
                    }
                    if (getRow == 11) {
                        break;
                    }
                    List<Object> col = new ArrayList<>();
                    for (int j = 0; j < lastCellNum; j++) {
                        //System.out.println("坐标:"+i+","+j);
                        //获取当前单元格
                        Cell cell = Objects.nonNull(row.getCell(j)) ? row.getCell(j) : row.createCell(j);
                        Object obj = getCellFormatValue(cell);
                        obj = (obj instanceof Date) ? simpleDateFormat.format((Date) obj) : obj;
                        col.add(obj);
                    }
                    long count = col.stream().count();
                    Optional.of(col).filter(x -> count > 0).ifPresent(content::add);
                    getRow++;
                }
                return content;
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return null;
    }

//    /**
//     * 根据excel单元格的日期格式，返回处理过的SimpleDateFormat todo:这样行不通
//     *
//     * @param formatdDate)
//     * @return
//     */
//    private static SimpleDateFormat dealWithDateCellType(String formatdDate) {
////        String formatString = BuiltinFormats.getBuiltinFormat(formatID);
////        DataFormatter dataFormatter = new DataFormatter();
//        String[] split = formatdDate.split(":");
//        String hour = null;
//        String minute = null;
//        String second = null;
//        for (int i = 0; i < split.length; i++) {
//            if (i == 0) {
//                hour = split[i].substring(split[i].indexOf(" ") + 1);
//            } else if (i == 1) {
//                if (Integer.parseInt(split[i]) == 0) {
//                    minute = null;
//                } else {
//                    minute = split[i];
//                }
//            } else if (i == 2) {
//                if (Integer.parseInt(split[i]) == 0) {
//                    second = null;
//                } else {
//                    second = split[i];
//                }
//            }
//        }
//        SimpleDateFormat sdf = null;
//        if (formatdDate.contains("00:00:00")) {
//            sdf = new SimpleDateFormat("yyyy-MM-dd");
//        } else if (StringUtils.isEmpty(second)) {
//            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//        } else if (StringUtils.isEmpty(minute) && StringUtils.isEmpty(second)) {
//            sdf = new SimpleDateFormat("yyyy-MM-dd HH");
//        } else if (StringUtils.isEmpty(hour) && StringUtils.isEmpty(minute) && StringUtils.isEmpty(second)) {
//            sdf = new SimpleDateFormat("yyyy-MM-dd");
//        } else {
//            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        }
//        return sdf;
//    }

    /**
     * @return java.util.List<java.util.List < java.lang.String>>
     * @description 读取csv内容，返回list，每一行存放一个list
     * @author Lock
     * @date 2022/1/5 11:18
     * @version v1.0
     * @params wb
     * @params index
     */
    private static List<List<Object>> readCsvContentList(InputStream inputStream) {
        // 默认只查询十行
        List<List<Object>> content = new ArrayList<>();
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
     * @return java.lang.Object
     * @description 根据Cell类型设置数据
     * @author Lock
     * @date 2021/12/28 10:25
     * @version v1.0
     * @params cell excel单元格对象
     */
    private static Object getCellFormatValue(Cell cell) {
        Object cellvalue = "";
        if (cell != null) {
            switch (cell.getCellType()) {
                //字符串
                case STRING:
                    cellvalue = cell.getStringCellValue();
                    break;
                //数值类型 - 整数、小数、日期
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        //该方法是直接获取单元格的真实值，并非日期值，而是获取的公式，不符合要求
//                        double excelValue = cell.getNumericCellValue();

                        //2023-06-05 李世纪解决poi读取excel表格中的日期数据时，因为代码读取的数值精度过高，导致的时间数值精度损失问题
                        //至少保证预览没问题
                        double excelValue = cell.getNumericCellValue();
                        long timeInMilliSeconds = (long) ((excelValue - 25569) * 86400 * 1000);
                        //减去时差
                        cellvalue = new Date(timeInMilliSeconds + 1 - TimeZone.getDefault().getRawOffset());

                        //直接调用该方法会导致精度损失
//                        cellvalue = cell.getDateCellValue();
                    } else {
                        //浮点数，excel是什么值，就存储什么值
                        cellvalue = NumberToTextConverter.toText(cell.getNumericCellValue());
//                        cellvalue = cell.getNumericCellValue();
                    }
                    break;
                //布尔值
                case BOOLEAN:
                    cellvalue = cell.getBooleanCellValue();
                    break;
                //公式
                case FORMULA:
                    switch (cell.getCachedFormulaResultType()) {
                        case STRING:
                            cellvalue = cell.getStringCellValue();
                            break;
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                //该方法可以直接获取excel单元格里的真正值（并非显示的数值，而是表达式）
//                                cellvalue = new DataFormatter().formatCellValue(cell);

                                //2023-06-05 李世纪解决poi读取excel表格中的日期数据时，因为代码读取的数值精度过高，导致的时间数值精度损失问题
                                //至少保证预览没问题
                                double excelValue = cell.getNumericCellValue();
                                long timeInMilliSeconds = (long) ((excelValue - 25569) * 86400 * 1000);
                                //减去时差
                                cellvalue = new Date(timeInMilliSeconds + 1 - TimeZone.getDefault().getRawOffset());

                                //直接调用该方法会导致精度损失
//                                cellvalue = cell.getDateCellValue();
                            } else {
                                //浮点数，excel是什么值，就存储什么值
                                cellvalue = NumberToTextConverter.toText(cell.getNumericCellValue());
//                                cellvalue = cell.getNumericCellValue();
                            }
                            break;
                        case BOOLEAN:
                            cellvalue = cell.getBooleanCellValue();
                            break;
                        default:
                            break;
                    }
                    break;
                //空单元格- 没值，但有单元格样式
                case BLANK:
                    break;
                //错误单元格
                case ERROR:
                    break;
                //处理其他类型的值
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
    /*public static List<List<String>> readExcel(String filePath) {
        Workbook wb = read(filePath);
        // 默认获取第一个sheet页
        return readExcelContentList(wb, 0);
    }*/

    /**
     * @return java.util.List<com.fisk.dataaccess.dto.ftp.ExcelDTO>
     * @description 读取excel内容
     * @author Lock
     * @date 2021/12/28 10:29
     * @version v1.0
     * @params inputStream Excel文件流
     * @params ext 文件后缀名
     */
    public static List<ExcelDTO> readExcelFromInputStream(InputStream inputStream, String ext, Integer startRow) {
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
                List<List<Object>> lists = readExcelContentList(workbook, i, startRow);
                ExcelDTO excelDTO = new ExcelDTO();
                // excel预览内容 根据用户定义的起始行预览
                excelDTO.excelContent = lists;
                // excel字段列表
                if (!CollectionUtils.isEmpty(lists)) {
                    excelDTO.excelField = excelDTO.excelContent.get(0);
                }
                // sheet名称
                excelDTO.sheetName = workbook.getSheetName(i);
                finalListDto.add(excelDTO);
            });
        } catch (Exception e) {
            throw new FkException(ResultEnum.READ_EXCEL_CONTENT_ERROR, "读取excel内容失败！");
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
    public static List<ExcelDTO> readCsvFromInputStream(InputStream inputStream, String filename, Integer startRow) {
        List<ExcelDTO> listDto = null;
        try {
            listDto = new ArrayList<>();
            // 读取csv内容，返回list，每一行存放一个list
            List<List<Object>> lists = readCsvContentList(inputStream);
            ExcelDTO excelDTO = new ExcelDTO();
            // csv内容
            excelDTO.excelContent = lists.stream().skip(startRow).collect(Collectors.toList());
            // csv没有多sheet页,本方法中莫瑞诺指定文件名为sheet名
            excelDTO.sheetName = filename;
            // 字段列表
            excelDTO.excelField = excelDTO.excelContent.get(0);
            listDto.add(excelDTO);
        } catch (Exception e) {
            throw new FkException(ResultEnum.READ_CSV_CONTENT_ERROR);
        }
        return listDto;
    }
}

