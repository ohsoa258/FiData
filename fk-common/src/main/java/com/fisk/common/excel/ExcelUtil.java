package com.fisk.common.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gy
 */
@Slf4j
public class ExcelUtil {

    /**
     * 用户信息导出类
     *
     * @param response 响应
     * @param fileName 文件名
     * @param dataList 导出的数据
     */
    public static void uploadExcelAboutUser(HttpServletResponse response, String fileName, List<Map<String, Object>> dataList) {
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
                            cell.setCellValue((Double) item.getValue());
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
