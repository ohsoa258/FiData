package com.fisk.task.dto.modelpublish;

import io.swagger.annotations.ApiModelProperty;
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
     * 创建表方式 2:维度 1:事实 3: 数据接入
     */
    public int createType;
    public String sqlScript;
    public List<ModelPublishFieldDTO> fieldList;
    public String groupComponentId;
    public String nifiCustomWorkflowDetailId;

    /*
     * 查询范围开始时间
     * */
    public String queryStartTime;

    /*
     * 查询范围结束时间
     * */
    public String queryEndTime;

    /*
    * 同步方式
    * */
    public Integer synMode;


    public String groupStructureId;

    @ApiModelProperty(value = "事实表-维度键的更新sql集合")
    public String factUpdateSql;

    /**
     * 单个数据流文件加载最大数据行
     */
    public int maxRowsPerFlowFile;
    /**
     * 单次从结果集中提取的最大数据行
     */
    public int fetchSize;
}
