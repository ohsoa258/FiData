package com.fisk.dataaccess.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Lock
 */
@Data
public class TableBusinessDTO {

    public long id;

    /**
     * tb_table_access(id)
     */
    public Long accessId;

    /**
     * 业务时间字段
     */
    public String businessTimeField;

    /**
     * 1:  取上一个月数据,覆盖上一个月数据
     * 2:  取当月数据,覆盖当月数据
     * 3:  当月
     * 4:  取上一年数据,覆盖上一年
     * 5:  取当年数据,覆盖当年
     */
    public Long businessFlag;

    /**
     * 当月具体多少号
     */
    public Long businessDay;
}
