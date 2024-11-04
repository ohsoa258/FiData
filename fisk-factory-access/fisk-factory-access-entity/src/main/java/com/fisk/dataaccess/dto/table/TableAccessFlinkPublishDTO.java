package com.fisk.dataaccess.dto.table;

import com.fisk.common.core.baseObject.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 * <p>
 * 非实时对象
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TableAccessFlinkPublishDTO extends BaseDTO {

    @ApiModelProperty(value = "物理表id")
    private Long id;

    /**
     * Flink Source Sql
     */
    @ApiModelProperty(value = "Flink Source Sql")
    private String sourceSql;

    /**
     * Flink Sink Sql
     */
    @ApiModelProperty(value = "Flink Sink Sql")
    private String sinkSql;

    /**
     * Flink Insert Sql
     */
    @ApiModelProperty(value = "Flink Insert Sql")
    private String insertSql;

}
