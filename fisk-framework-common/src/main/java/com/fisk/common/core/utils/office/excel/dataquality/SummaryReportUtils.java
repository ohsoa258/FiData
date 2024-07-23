package com.fisk.common.core.utils.office.excel.dataquality;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.utils.Dto.Excel.ExcelDto;
import com.fisk.common.core.utils.Dto.Excel.RowDto;
import com.fisk.common.core.utils.Dto.Excel.SheetDto;
import com.fisk.common.core.utils.Dto.Excel.dataquality.QualityReportSummaryDTO;
import com.fisk.common.core.utils.Dto.Excel.dataquality.QualityReportSummary_BodyDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据质量-总结报告
 * @date 2024/7/19 11:56
 */
@Slf4j
public class SummaryReportUtils {

    /**
     * @return void
     * @description 生成summary报告
     * @author dick
     * @date 2024/7/19 16:22
     * @version v1.0
     * @params qualityReportSummaryList
     * @params uploadUrl
     * @params fileName
     * @params isMergeRow
     */
    public static void createSummaryQualityReport(List<QualityReportSummaryDTO> qualityReportSummaryList, String uploadUrl, String fileName) {
        //1.创建workbook
        XSSFWorkbook workbook = new XSSFWorkbook();
        FileOutputStream fos = null;
        try {
            //2.根据workbook创建sheet
            createSheet(workbook, qualityReportSummaryList);
            //3.通过输出流写到文件里去
            File f = new File(uploadUrl);
            if (!f.exists()) {
                f.mkdirs();
            }
            String filePath = uploadUrl + fileName;
            fos = new FileOutputStream(filePath);
            workbook.write(fos);
        } catch (IOException e) {
            log.error("Excel生成异常，ex", e);
        } finally {
            try {
                // 关闭输出流
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                log.error("Excel生成异常 流关闭失败，ex", e);
            }
        }
    }

    /**
     * @return void
     * @description 生成Sheet
     * @author dick
     * @date 2024/7/19 16:24
     * @version v1.0
     * @params workbook
     * @params qualityReportSummaryList
     */
    public static void createSheet(XSSFWorkbook workbook, List<QualityReportSummaryDTO> qualityReportSummaryList) {
        // 创建一个sheet
        XSSFSheet sheet = workbook.createSheet();
        workbook.setSheetName(0, "Summary of Quality Report");

        // 创建sheet的行样式
        HashMap<String, CellStyle> cellStyle = getCellStyle(workbook);
        CellStyle style_epilogue = cellStyle.get("style_epilogue");
        CellStyle style_header = cellStyle.get("style_header");
        CellStyle style_data = cellStyle.get("style_data");

        // 设置单元格列宽
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(2, 6000);
        sheet.setColumnWidth(3, 8000);
        sheet.setColumnWidth(4, 10000);
        sheet.setColumnWidth(5, 4000);
        sheet.setColumnWidth(6, 4000);
        sheet.setColumnWidth(7, 4000);

        // 合并前两行，用来描述结语
        String epilogue = qualityReportSummaryList.get(0).getEpilogue();
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 7));
        Row conclusionRow = sheet.createRow(0);
        Cell conclusionCell = conclusionRow.createCell(0);
        conclusionCell.setCellValue(epilogue);
        conclusionCell.setCellStyle(style_epilogue);

        // 创建列头
        Row headerRow = sheet.createRow(3);
        headerRow.setHeightInPoints(20);
        String[] headers = {"报告名称", "报告负责人", "报告批次号", "表名称", "检查规则名称", "检查数据条数", "数据的正确率", "是否通过检查"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style_header);
        }

        // 创建数据行
        int dataIndex = 4;
        if (CollectionUtils.isNotEmpty(qualityReportSummaryList)) {
            for (QualityReportSummaryDTO qualityReportSummaryDTO : qualityReportSummaryList) {
                for (QualityReportSummary_BodyDTO qualityReportSummary_bodyDTO : qualityReportSummaryDTO.qualityReportSummary_body) {
                    Row dataRow = sheet.createRow(dataIndex);
                    createCell(dataRow, 0, qualityReportSummaryDTO.getReportName(), style_data);
                    createCell(dataRow, 1, qualityReportSummaryDTO.getReportPrincipal(), style_data);
                    createCell(dataRow, 2, qualityReportSummaryDTO.getReportBatchNumber(), style_data);
                    createCell(dataRow, 3, qualityReportSummaryDTO.getTableFullName(), style_data);
                    createCell(dataRow, 4, qualityReportSummary_bodyDTO.getRuleName(), style_data);
                    createCell(dataRow, 5, qualityReportSummary_bodyDTO.getCheckDataCount(), style_data);
                    createCell(dataRow, 6, qualityReportSummary_bodyDTO.getDataAccuracy(), style_data);
                    createCell(dataRow, 7, qualityReportSummary_bodyDTO.getCheckStatus(), style_data);
                    dataIndex++;
                }
            }
        }

        // 开始合并行、结束合并行、开始合并列（单元格）、结束合并列（单元格）
        int firstRow = 4, lastRow = 3;
        // 合并数据行，根据表名称合并
        if (CollectionUtils.isNotEmpty(qualityReportSummaryList)) {
            for (QualityReportSummaryDTO qualityReportSummaryDTO : qualityReportSummaryList) {
                int tableRuleCount = qualityReportSummaryDTO.getQualityReportSummary_body().size();
                lastRow += tableRuleCount;
                setSheetCellRangeMerge(workbook, sheet, firstRow, lastRow, 3, 3); // 合并表名称
                firstRow = lastRow + 1;
            }
        }
        // 合并报告名称、报告负责人、报告批次号
        setSheetCellRangeMerge(workbook, sheet, 4, lastRow, 0, 0); // 合并报告名称
        setSheetCellRangeMerge(workbook, sheet, 4, lastRow, 1, 1); // 合并报告负责人
        setSheetCellRangeMerge(workbook, sheet, 4, lastRow, 2, 2); // 合并报告批次号
    }

    /**
     * @return void
     * @description 创建单元格并赋值
     * @author dick
     * @date 2024/7/19 17:03
     * @version v1.0
     * @params row
     * @params column
     * @params value
     * @params style
     */
    public static void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        }
        cell.setCellStyle(style);
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
        // 创建结语样式
        CellStyle style_epilogue = workbook.createCellStyle();
        // 设置字体
        Font epilogueFont = workbook.createFont();
        epilogueFont.setFontName("宋体");
        epilogueFont.setFontHeightInPoints((short) 11);
        epilogueFont.setBold(true);
        style_epilogue.setFont(epilogueFont);
        // 设置居中 水平居中/左对齐
        style_epilogue.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style_epilogue.setVerticalAlignment(HSSFCellStyle.ALIGN_LEFT);
        // 设置自动换行
        style_epilogue.setWrapText(true);
        cellStyleHashMap.put("style_epilogue", style_epilogue);

        // 创建列头样式
        CellStyle style_header = workbook.createCellStyle();
        // 设置背景色
        style_header.setFillForegroundColor(IndexedColors.TAN.getIndex());
        style_header.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        // 设置字体
        Font headerFont = workbook.createFont();
        headerFont.setFontName("宋体");
        headerFont.setFontHeightInPoints((short) 11);
        style_header.setFont(headerFont);
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
        // 设置边框
        style_data.setBorderBottom((short) 1);
        style_data.setBorderLeft((short) 1);
        style_data.setBorderRight((short) 1);
        style_data.setBorderTop((short) 1);
        // 设置居中 水平居中/左对齐
        style_data.setAlignment(HSSFCellStyle.ALIGN_CENTER);
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
     * @date 2024/7/19 17:13
     * @version v1.0
     * @params workbook
     * @params sheet
     * @params firstRow
     * @params lastRow
     * @params firstCol
     * @params lastCol
     */
    public static void setSheetCellRangeMerge(Workbook workbook, Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        // 起始行号，终止行号， 起始列号，终止列号
        CellRangeAddress region = new CellRangeAddress(firstRow, lastRow, (short) firstCol, (short) lastCol);
        // 使用RegionUtil类为合并后的单元格添加边框
        RegionUtil.setBorderBottom(HSSFCellStyle.BORDER_THIN, region, sheet, workbook); // 下边框
        RegionUtil.setBorderLeft(HSSFCellStyle.BORDER_THIN, region, sheet, workbook); // 左边框
        RegionUtil.setBorderRight(HSSFCellStyle.BORDER_THIN, region, sheet, workbook); // 右边框
        RegionUtil.setBorderTop(HSSFCellStyle.BORDER_THIN, region, sheet, workbook); // 上边框
        sheet.addMergedRegion(region);
    }
}
