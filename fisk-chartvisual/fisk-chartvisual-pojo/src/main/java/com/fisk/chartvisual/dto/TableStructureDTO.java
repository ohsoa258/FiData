package com.fisk.chartvisual.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author WangYan
 * @date 2022/3/9 10:34
 */
@Data
public class TableStructureDTO {

    /**
     * 数据源id
     */
    @NotNull
    private Integer id;
    /**
     * 表名
     */
    @NotNull
    private List<String> tableName;
}
