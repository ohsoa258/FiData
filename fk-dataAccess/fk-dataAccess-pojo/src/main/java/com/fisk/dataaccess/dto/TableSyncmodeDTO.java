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
public class TableSyncmodeDTO extends BaseDTO {

    /**
     * id
     */
    public long id;

    /**
     * 1：全量、2：时间戳增量、3：业务时间覆盖、4：自定义覆盖；
     */
    public int syncMode;

    /**
     * 时间戳字段
     */
    public String syncField;

    /**
     * 自定义删除条件：定义每次同步的时候删除我们已有的数据条件
     */
    public String customDeleteCondition;

    /**
     * 自定义插入条件：定义删除之后获取插入条件的数据进行插入
     */
    public String customInsertCondition;

    /**
     * timer driver
     */
    public String timerDriver;

    /**
     * corn表达式
     */
    public String cornExpression;

    public TableSyncmodeDTO(BaseEntity entity) {
        super(entity);
    }

    public static <T extends BaseEntity> List<TableSyncmodeDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(TableSyncmodeDTO::new).collect(Collectors.toList());
    }

}
