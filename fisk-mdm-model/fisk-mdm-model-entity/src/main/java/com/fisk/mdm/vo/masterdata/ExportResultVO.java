package com.fisk.mdm.vo.masterdata;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author JianWenYang
 * @date 2022/5/5 14:17
 */
@Data
public class ExportResultVO {

    /**
     * 导出文件名
     */
    private String fileName;
    /**
     * 查询数据集
     */
    private List<Map<String, Object>> dataArray;
    /**
     * 表头集合
     */
    private List<String> headerList;

    private List<String> headerDisplayList;

}
