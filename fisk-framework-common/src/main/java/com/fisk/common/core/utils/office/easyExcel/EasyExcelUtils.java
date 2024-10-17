package com.fisk.common.core.utils.office.easyExcel;

import com.alibaba.excel.EasyExcel;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
public class EasyExcelUtils {

//    /**
//     * 数据湖管理CDC导出数据 - 排除指定列
//     *
//     * @param response
//     * @param sheetName               文件名称
//     * @param excludeColumnFieldNames 需要忽略的字段（excel不需要的字段）
//     * @param data                    数据
//     */
//    public static void AccessCdcExcludeWrite(HttpServletResponse response, String sheetName,
//                                             List<String> excludeColumnFieldNames, List<AccessCDCExcelDTO> data) {
//        try {
//            EasyExcel.write(response.getOutputStream(), AccessCDCExcelDTO.class)
//                    .excludeColumnFieldNames(excludeColumnFieldNames)
//                    .sheet(sheetName)
//                    .doWrite(data);
//        } catch (IOException e) {
//            log.error("Excel导出失败", e);
//            throw new FkException(ResultEnum.EXCEL_EXPORT_ERROR);
//        }
//    }

    /**
     * 数据湖管理CDC导出数据 - 只导出指定列
     *
     * @param response
     * @param sheetName               文件名称
     * @param includeColumnFieldNames 需要忽略的字段（excel不需要的字段）
     * @param data                    数据
     */
    public static void AccessCDCIncludeWrite(HttpServletResponse response, String sheetName,
                                             List<String> includeColumnFieldNames, List<AccessCDCExcelDTO> data) {
        try {
            EasyExcel.write(response.getOutputStream(), AccessCDCExcelDTO.class)
                    .includeColumnFieldNames(includeColumnFieldNames)
                    .sheet(sheetName)
                    .doWrite(data);
        } catch (IOException e) {
            log.error("Excel导出失败", e);
            throw new FkException(ResultEnum.EXCEL_EXPORT_ERROR);
        }
    }


}
