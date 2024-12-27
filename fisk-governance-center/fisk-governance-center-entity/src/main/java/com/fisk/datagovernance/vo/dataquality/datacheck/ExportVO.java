package com.fisk.datagovernance.vo.dataquality.datacheck;

import lombok.Data;

import java.util.List;

/**
 * @author wangjian
 * @date 2024/12/26 10:21
 */
@Data
public class ExportVO {

    /**
     * 导出文件名
     */
    private String fileName;
    /**
     * 查询数据集
     */
    private List<DataCheckLogExcelVO> dataArray;
    /**
     * 表头集合
     */
    private List<String> headerList;

    private List<String> headerDisplayList;

}
