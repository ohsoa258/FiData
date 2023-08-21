package com.fisk.common.core.utils.office.excel;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.server.metadata.TemplateAttributeDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author gy
 */
@Slf4j
public class ExcelUtil {

    private static final String[] parentMetaDataHeaders = {"名称", "显示名称", "元数据类型", "描述"};
    private static final String[] mainMetaDataHeaders = {"一级分类", "二级分类", "应用类型", "名称", "显示名称", "元数据类型", "描述", "校验规则"};
    private static final String[] childMetaDataHeaders = {"名称", "显示名称", "元数据类型", "描述", "类型", "长度", "校验规则"};

    /**
     * 用户信息导出类
     *
     * @param response 响应
     * @param fileName 文件名
     * @param dataList 导出的数据
     */
    public static void uploadExcelAboutUser(HttpServletResponse response, String fileName,
                                            List<Map<String, Object>> dataList) {
        //声明输出流
        OutputStream os = null;
        try {
            //设置响应头
            setResponseHeader(response, fileName);
            //获取输出流
            os = response.getOutputStream();
            //内存中保留1000条数据，以免内存溢出，其余写入硬盘
            SXSSFWorkbook wb = new SXSSFWorkbook(1000);
            //获取该工作区的第一个sheet
            Sheet sheet1 = wb.createSheet("sheet1");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            if (dataList != null && dataList.size() > 0) {
                int excelRow = 0;
                //获取字段信息
                Map<String, Class<?>> columnType = getColumnType(dataList.get(0));

                //创建标题行
                Row titleRow = sheet1.createRow(excelRow++);
                int index = 0;
                for (String item : columnType.keySet()) {
                    //创建该行下的每一列，并写入标题数据
                    Cell cell = titleRow.createCell(index);
                    cell.setCellValue(item);
                    index++;
                }
                index = 0;

                //设置内容行
                for (Map<String, Object> row : dataList) {
                    Row dataRow = sheet1.createRow(excelRow++);
                    //内层for循环创建每行对应的列，并赋值
                    int columnIndex = 0;
                    for (Map.Entry<String, Object> item : row.entrySet()) {
                        Cell cell = dataRow.createCell(columnIndex);
                        columnIndex++;
                        if (item.getValue() == null) {
                            continue;
                        }
                        Class<?> type = columnType.get(item.getKey());
                        if (Integer.class.equals(type)) {
                            cell.setCellValue(((Integer) item.getValue()).doubleValue());
                        } else if (Long.class.equals(type)) {
                            cell.setCellValue(new Double((Long) item.getValue()));
                        } else if (String.class.equals(type)) {
                            cell.setCellValue((String) item.getValue());
                        } else if (Date.class.equals(type)) {
                            cell.setCellValue((Date) item.getValue());
                        } else if (Timestamp.class.equals(type)) {
                            cell.setCellValue(sdf.format((Timestamp) item.getValue()));
                        } else if (BigDecimal.class.equals(type)) {
                            cell.setCellValue(((BigDecimal) item.getValue()).doubleValue());
                        } else if (Double.class.equals(type)) {
                            cell.setCellValue((Double) item.getValue());
                        }
                    }
                }
                //将整理好的excel数据写入流中
                wb.write(os);
            }
        } catch (IOException e) {
            log.error("Excel导出失败，ex", e);
        } finally {
            try {
                // 关闭输出流
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                log.error("Excel导出 流关闭失败，ex", e);
            }
        }
    }

    /**
     * @return java.io.InputStream
     * @description 创建保存excel
     * @author dick
     * @date 2022/4/15 17:22
     * @version v1.0
     * @params filePath 文件全路径，含文件名称
     * @params dataList
     */
    public static ResultEnum createSaveExcel(String filePath, String sheetName,
                                             List<Map<String, Object>> dataList) {
        if (filePath == null || filePath.isEmpty() ||
                dataList == null || dataList.size() <= 0) {
            return ResultEnum.PARAMTER_ERROR;
        }
        if (sheetName == null || sheetName.isEmpty()) {
            sheetName = "sheet1";
        }
        OutputStream outputStreamExcel = null;
        try {
            File tmpFile = new File(filePath);
            if (!tmpFile.getParentFile().exists()) {
                tmpFile.getParentFile().mkdirs();//创建目录
            }
            if (!tmpFile.exists()) {
                try {
                    tmpFile.createNewFile();//创建文件
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //创建Workbook对象(excel的文档对象) 导出的Excel行数为104万行，是操作Excel2007后的版本，扩展名是.xlsx；
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
            XSSFSheet sheet = xssfWorkbook.createSheet(sheetName);

            // 设置通用样式
            XSSFCellStyle style = xssfWorkbook.createCellStyle();
            style.setAlignment(HorizontalAlignment.CENTER); //居中
            style.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
            style.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
            style.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
            style.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
            style.setWrapText(true); //自动换行
            style.setHidden(true);//高度自动
            style.setFillBackgroundColor(HSSFColor.PALE_BLUE.index); //背景颜色
            XSSFFont font = xssfWorkbook.createFont();
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            font.setFontHeight(11);
            style.setFont(font);
            sheet.setDefaultColumnWidth(10); //设置宽度

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            //获取字段信息
            Map<String, Class<?>> columnType = getColumnType(dataList.get(0));

            //写入表头
            int excelRow = 0;
            XSSFRow headerRow = sheet.createRow(excelRow++);
            int index = 0;
            for (String headerName : columnType.keySet()) {
                XSSFCell cell = headerRow.createCell(index);
                cell.setCellValue(headerName);
                cell.setCellStyle(style);
                index++;
            }

            //写入数据
            for (Map<String, Object> row : dataList) {
                Row dataRow = sheet.createRow(excelRow++);
                //内层for循环创建每行对应的列，并赋值
                int columnIndex = 0;
                for (Map.Entry<String, Object> item : row.entrySet()) {
                    Cell cell = dataRow.createCell(columnIndex);
                    columnIndex++;
                    if (item.getValue() == null) {
                        continue;
                    }
                    Class<?> type = columnType.get(item.getKey());
                    if (Integer.class.equals(type)) {
                        cell.setCellValue(((Integer) item.getValue()).doubleValue());
                    } else if (Long.class.equals(type)) {
                        cell.setCellValue(new Double((Long) item.getValue()));
                    } else if (String.class.equals(type)) {
                        cell.setCellValue((String) item.getValue());
                    } else if (Date.class.equals(type)) {
                        cell.setCellValue((Date) item.getValue());
                    } else if (Timestamp.class.equals(type)) {
                        cell.setCellValue(sdf.format((Timestamp) item.getValue()));
                    } else if (BigDecimal.class.equals(type)) {
                        cell.setCellValue(((BigDecimal) item.getValue()).doubleValue());
                    } else if (Double.class.equals(type)) {
                        cell.setCellValue((Double) item.getValue());
                    }
                }
            }

            //写入流到文件
            outputStreamExcel = new FileOutputStream(tmpFile);
            xssfWorkbook.write(outputStreamExcel);
        } catch (Exception ex) {
            log.error("CreateSaveExcel异常：" + ex);
            throw new FkException(ResultEnum.ERROR, ex);
        } finally {
            try {
                // 关闭输出流
                if (outputStreamExcel != null) {
                    outputStreamExcel.flush();
                    outputStreamExcel.close();
                }
            } catch (IOException ex) {
                log.error("CreateSaveExcel 流关闭异常：", ex);
            }
        }
        return ResultEnum.SUCCESS;
    }


    /**
     * @return java.io.InputStream
     * @description 创建保存excel
     * @author dick
     * @date 2022/4/15 17:22
     * @version v1.0
     * @params filePath 文件全路径，含文件名称
     * @params dataList
     */
    public static InputStream createMetaDataSaveExcel(
            String sheetName,
            List<Map<String, Object>> dataList,
            List<TemplateAttributeDto> commonAttributeDtoList) {
        InputStream stream = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        try {

            XSSFSheet sheet = xssfWorkbook.createSheet(sheetName);

            // 设置通用样式
            XSSFCellStyle style = xssfWorkbook.createCellStyle();
            style.setAlignment(HorizontalAlignment.CENTER); //居中
            style.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
            style.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
            style.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
            style.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
            style.setWrapText(true); //自动换行
            style.setHidden(true);//高度自动
            style.setFillBackgroundColor(HSSFColor.PALE_BLUE.index); //背景颜色
            XSSFFont font = xssfWorkbook.createFont();
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            font.setFontHeight(11);
            style.setFont(font);
            sheet.setDefaultColumnWidth(10); //设置宽度

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


            //单个父级元数据元素个数
            Integer parentMetadataAttributeNumber = parentMetaDataHeaders.length;
            //写入表头
            setMetadataSheetTableHeader(sheet, commonAttributeDtoList);
            //获取二级表头 ,排除一级表头
            List<TemplateAttributeDto> twoLevelAttribute = commonAttributeDtoList.stream().filter(e -> !e.getAttributePid().equals(0))
                    .sorted(Comparator.comparing(TemplateAttributeDto::getAttributeId))
                    .collect(Collectors.toList());

            //起始行
            int excelRow = 2;
            //写入数据
            for (Map<String, Object> row : dataList) {
                Row dataRow = sheet.createRow(excelRow++);
                //内层for循环创建每行对应的列，并赋值
                int columnIndex = 0;
                for (TemplateAttributeDto attributeDto : twoLevelAttribute) {
                    Cell cell = dataRow.createCell(columnIndex);
                    columnIndex++;
                    String key=attributeDto.getAttributeId()+"";
                    if (row.containsKey(key)) {
                        cell.setCellValue((String) row.get(key));
                    }
                }
            }
            xssfWorkbook.write(outputStream);
            stream = new ByteArrayInputStream(outputStream.toByteArray());
        } catch (Exception ex) {
            log.error("CreateSaveExcel异常：" + ex);
            throw new FkException(ResultEnum.ERROR, ex);
        } finally {
            try {
                xssfWorkbook.close();
                // 关闭输出流
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException ex) {
                log.error("CreateSaveExcel 流关闭异常：", ex);
            }
        }

        return stream;
    }

    private static void setMetadataSheetTableHeader(Sheet sheet, List<TemplateAttributeDto> commonAttributeDtoList) {
        //一级表头起始下标
        Integer startIndex = 0;
        Row row1 = sheet.createRow(0);
        Row row2 = sheet.createRow(1);
        //一级表头ID
        Integer[] oneLevelHeader = {-2,-1 , 3, 4};
        for (Integer oneLevelId : oneLevelHeader) {
            //获取一级表头信息
            TemplateAttributeDto oneLevelAttribute = commonAttributeDtoList.stream().filter(e -> e.getAttributeId().equals(oneLevelId)).findFirst().orElse(null);
            if (oneLevelAttribute != null) {
                //获取二级表头信息
                List<TemplateAttributeDto> twoLevelAttributeList = commonAttributeDtoList.stream()
                        .filter(e -> e.getAttributePid().equals(oneLevelId))
                        .sorted(Comparator.comparing(TemplateAttributeDto::getAttributeId))
                        .collect(Collectors.toList());
                //判断是否显示二级表头
                Integer twoAttributeCount = Long.valueOf(twoLevelAttributeList.stream().count()).intValue();
                if (twoAttributeCount > 0) {
                    //设置excel 一级表头 合并一级表头
                    Integer firstCol = startIndex;
                    Integer lastRow = startIndex + twoAttributeCount - 1;
                    CellRangeAddress mergedRegion = new CellRangeAddress(0, 0, firstCol, lastRow);
                    sheet.addMergedRegion(mergedRegion);
                    Cell cellA1 = row1.createCell(firstCol);
                    cellA1.setCellValue(oneLevelAttribute.getAttributeName());
                    //设置二级表头
                    for (TemplateAttributeDto towAttributeDto : twoLevelAttributeList) {
                        Cell cellA2 = row2.createCell(startIndex);
                        cellA2.setCellValue(towAttributeDto.getAttributeName());
                        startIndex++;
                    }
                }

            }
        }
    }

    /**
     * 获取行的字段名和类型
     *
     * @param row 行
     * @return 字段信息
     */
    private static Map<String, Class<?>> getColumnType(Map<String, Object> row) {
        Map<String, Class<?>> map = new HashMap<>();
        for (Map.Entry<String, Object> item : row.entrySet()) {
            map.put(item.getKey(), item.getValue().getClass());
        }
        return map;
    }

    /**
     * 设置浏览器下载响应头
     */
    private static void setResponseHeader(HttpServletResponse response, String fileName) {
        try {
            try {
                fileName = new String(fileName.getBytes(), "ISO8859-1");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.addHeader("Pargam", "no-cache");
            response.addHeader("Cache-Control", "no-cache");
        } catch (Exception ex) {
            log.error("设置响应对象失败，ex", ex);
        }
    }
}
