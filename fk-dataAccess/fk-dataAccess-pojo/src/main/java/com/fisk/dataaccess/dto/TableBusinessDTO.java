package com.fisk.dataaccess.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TableBusinessDTO {
    @ApiModelProperty(value = "业务域覆盖id")
    public long id;

    /**
     * tb_table_access(id)
     */
    @ApiModelProperty(value = "物理表id",required = true)
    public Long accessId;

    /**
     * 业务时间字段
     */
    @ApiModelProperty(value = "业务时间字段",required = true)
    public String businessTimeField;

    /**
     * 1:  取上一个月数据,覆盖上一个月数据
     * 2:  取当月数据,覆盖当月数据
     * 3:  当月
     * 4:  取上一年数据,覆盖上一年
     * 5:  取当年数据,覆盖当年
     */
    @ApiModelProperty(value = "     * 1:  取上一个月数据,覆盖上一个月数据\n" +
            "     * 2:  取当月数据,覆盖当月数据\n" +
            "     * 3:  当月\n" +
            "     * 4:  取上一年数据,覆盖上一年\n" +
            "     * 5:  取当年数据,覆盖当年",required = true)
    public Long businessFlag;

    /**
     * 当月具体多少号
     */
    @ApiModelProperty(value = "当月具体多少号",required = true)
    public Long businessDay;
}
