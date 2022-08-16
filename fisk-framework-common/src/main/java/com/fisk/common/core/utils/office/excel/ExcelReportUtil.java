package com.fisk.common.core.utils.office.excel;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.utils.Dto.Excel.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description Exel报告
 * @date 2022/8/15 16:32
 */
@Slf4j
public class ExcelReportUtil {

    /**
     * @return void
     * @description 生成excel
     * @author dick
     * @date 2022/8/15 16:40
     * @version v1.0
     * @params excelDto
     * @params uploadUrl
     * @params fileName
     */
    public static void createExcel(ExcelDto excelDto, String uploadUrl, String fileName) {
        //1.创建workbook
        XSSFWorkbook workbook = new XSSFWorkbook();
        FileOutputStream fos = null;
        try {
            //2.根据workbook创建sheet
            for (int i = 0; i < excelDto.getSheets().size(); i++) {
                SheetDto sheet = excelDto.getSheets().get(i);
                createSheet(workbook, i, sheet.getSheetName(), sheet.getSingRows(), sheet.getDataRows());
            }
            //3.通过输出流写到文件里去
            File f = new File(uploadUrl);
            if (!f.exists()) {
                f.mkdirs();
            }
            String filePath = uploadUrl + fileName;
            fos = new FileOutputStream(filePath);
            workbook.write(fos);
        } catch (IOException e) {
            log.error("质量报告生成异常，ex", e);
        } finally {
            try {
                // 关闭输出流
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                log.error("质量报告生成 流关闭失败，ex", e);
            }
        }
    }

    /**
     * @param workbook
     * @param sheetNum  (sheet的位置，0表示第一个表格中的第一个sheet)
     * @param sheetName （sheet的名称）
     * @param headers   （表格的标题）
     * @param result    （表格的数据）
     * @return void
     * @description 生成Sheet
     * @author dick
     * @date 2022/8/11 10:52
     * @version v1.0
     */
    public static void createSheet(XSSFWorkbook workbook, int sheetNum,
                                   String sheetName, List<RowDto> headers, List<List<DataDto>> result) {
        // 创建一个sheet
        XSSFSheet sheet = workbook.createSheet();
        workbook.setSheetName(sheetNum, sheetName);
        sheet.setDefaultColumnWidth((short) 20);

        // 创建sheet的行样式
        HashMap<String, CellStyle> cellStyle = getCellStyle(workbook);
        CellStyle style_header = cellStyle.get("style_header");
        CellStyle style_data = cellStyle.get("style_data");

        XSSFRow row = null;
        XSSFCell cell = null;
        // 创建标识行
        for (int rowIndex = 0; rowIndex < headers.size(); rowIndex++) {
            RowDto rowEntity = headers.get(rowIndex);
            row = sheet.createRow(rowEntity.getRowIndex());
            row.setHeightInPoints(20);
            for (int cellIndex = 0; cellIndex < rowEntity.getColumns().size(); cellIndex++) {
                //ColumnDto column = rowEntity.getColumns().get(cellIndex);
                cell = row.createCell(cellIndex);
                cell.setCellStyle(style_header);
               // cell.setCellValue(column.getColumnName());
            }
        }
        // 创建数据行
        if (CollectionUtils.isNotEmpty(result)) {
            int maxRowIndex = headers.stream().max(Comparator.comparing(RowDto::getRowIndex)).get().getRowIndex();
            for (int rowIndex = 0; rowIndex < result.size(); rowIndex++) {
                maxRowIndex++;
                row = sheet.createRow(maxRowIndex);
                row.setHeightInPoints(15);
                List<DataDto> dataEntities = result.get(rowIndex);
                for (int cellIndex = 0; cellIndex < dataEntities.size(); cellIndex++) {
                    DataDto dataEntity = dataEntities.get(cellIndex);
                    cell = row.createCell(cellIndex);
                    cell.setCellStyle(style_data);
                    cell.setCellValue(dataEntity.getFieldValue());
                }
            }
        }
        // 合并标识行
        int lastCol = headers.stream().filter(t -> t.getRowIndex() == 4).findFirst().get().getColumns().size();
        setSheetCellRangeAddress(workbook, sheet, lastCol);
    }

    /**
     * @return java.util.HashMap<java.lang.String, org.apache.poi.ss.usermodel.CellStyle>
     * @description 行列样式
     * @author dick
     * @date 2022/8/11 15:10
     * @version v1.0
     * @params workbook
     */
    public static HashMap<String, CellStyle> getCellStyle(Workbook workbook) {
        HashMap<String, CellStyle> cellStyleHashMap = new HashMap<>();

        // 创建标识行样式
        CellStyle style_header = workbook.createCellStyle();
        // 设置背景色
        style_header.setFillForegroundColor(IndexedColors.TAN.getIndex());
        style_header.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        // 设置字体
        Font boldFont = workbook.createFont();
        boldFont.setFontName("宋体");
        boldFont.setFontHeightInPoints((short) 11);
        style_header.setFont(boldFont);
        // 设置边框
        style_header.setBorderBottom((short) 1);
        style_header.setBorderLeft((short) 1);
        style_header.setBorderRight((short) 1);
        style_header.setBorderTop((short) 1);
        // 设置居中 水平居中/垂直居中
        style_header.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style_header.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        // 设置自动换行
        style_header.setWrapText(true);
        cellStyleHashMap.put("style_header", style_header);

        // 创建数据行样式
        CellStyle style_data = workbook.createCellStyle();
        // 设置字体
        Font dataFont = workbook.createFont();
        dataFont.setFontName("宋体");
        dataFont.setFontHeightInPoints((short) 11);
        style_data.setFont(dataFont);
        // 设置居中
        style_data.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        style_data.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        // 设置自动换行
        style_data.setWrapText(true);
        cellStyleHashMap.put("style_data", style_data);

        return cellStyleHashMap;
    }

    /**
     * @return void
     * @description 行列合并
     * @author dick
     * @date 2022/8/11 15:09
     * @version v1.0
     * @params sheet
     * @params lastCol
     */
    public static void setSheetCellRangeAddress(Workbook workbook, Sheet sheet, int lastCol) {
        // 起始行号，终止行号， 起始列号，终止列号
        CellRangeAddress region = new CellRangeAddress(3, 3, (short) 0, (short) lastCol - 1);
        // 使用RegionUtil类为合并后的单元格添加边框
        RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, region, sheet, workbook); // 下边框
        RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, region, sheet, workbook); // 左边框
        RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, region, sheet, workbook); // 右边框
        RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, region, sheet, workbook); // 上边框
        sheet.addMergedRegion(region);
    }
}
