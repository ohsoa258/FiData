package com.fisk.common.service.mdmBEBuild.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author JianWenYang
 * @date 2022-05-16 19:36
 */
@Data
public class InsertImportDataDTO {

    /**
     * 导入模板数据
     */
    private List<Map<String, Object>> members;

    private String tableName;

    /**
     * 批次号
     */
    private String batchCode;

    private Integer versionId;

    private Long userId;

    /**
     * 导入类型
     */
    private Integer importType;

}
