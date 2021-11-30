package com.fisk.datamodel.dto.modelpublish;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ModelPublishTableDTO {
    public long tableId;
    public String tableName;
    /**
     * 创建表方式 0:维度 1:事实
     */
    public int createType;
    public String sqlScript;
    public List<ModelPublishFieldDTO> fieldList;
    public String groupComponentId;
    public String nifiCustomWorkflowDetailId;
}
