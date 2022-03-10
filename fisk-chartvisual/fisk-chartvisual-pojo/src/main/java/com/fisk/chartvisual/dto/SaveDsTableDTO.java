package com.fisk.chartvisual.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author WangYan
 * @date 2022/3/9 17:27
 */
@Data
public class SaveDsTableDTO {

    /**
     * 数据源id
     */
    @NotNull
    private Integer dataSourceId;
    /**
     * 表名
     */
    @NotNull
    private String tableName;

    /**
     * 字段信息
     */
    private List<DsTableFieldDTO> fieldList;
}
