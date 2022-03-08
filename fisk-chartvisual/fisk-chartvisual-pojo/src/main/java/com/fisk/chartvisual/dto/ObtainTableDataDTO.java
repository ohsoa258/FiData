package com.fisk.chartvisual.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author WangYan
 * @date 2022/3/8 11:26
 */
@Data
public class ObtainTableDataDTO {

    /**
     * 数据源id
     */
    @NotNull
    private Integer id;
    /**
     * 表名
     */
    @NotNull
    private String tableName;
    /**
     * 分页条数
     */
    private Integer total;
}
