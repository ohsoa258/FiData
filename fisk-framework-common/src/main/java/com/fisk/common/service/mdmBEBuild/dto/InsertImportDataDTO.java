package com.fisk.common.service.mdmBEBuild.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 * @date 2022-05-16 19:36
 */
@Data
public class InsertImportDataDTO {

    /**
     * 导入模板数据
     */
    private List<JSONObject> members;

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
