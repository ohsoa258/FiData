package com.fisk.dataaccess.dto;

import com.fisk.common.dto.BaseDTO;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lock
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TableBusinessDTO extends BaseDTO {

    public long id;

    /**
     * tb_table_access(id)
     */
    public long accessId;

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
    public long businessFlag;

    /**
     * 当月具体多少号
     */
    public long businessDay;

    public TableBusinessDTO(BaseEntity entity) {
        super(entity);
    }

    public static <T extends BaseEntity> List<TableBusinessDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(TableBusinessDTO::new).collect(Collectors.toList());
    }

}
