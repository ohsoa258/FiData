package com.fisk.dataaccess.dto.table;

import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lock
 * <p>
 * 物理表接口首页对象
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TablePhyHomeDTO extends BaseDTO {

    /**
     * tb_table_access表id
     */
    public long id;

    /**
     *  物理表名
     */
    public String tableName;

    /**
     *  物理表描述
     */
    public String tableDes;

    /**
     * 数据列数
     */
    public int columnNum;

    /**
     * 数据行数
     */
    public int rowNum;

    /**
     * 同步方式:
     * 1：全量  2：时间戳增量  3：业务时间覆盖  4：自定义覆盖
     */
    public int syncMode;

    /**
     * 同步频率
     */
    public int syncFrequency;

    /**
     * 增量字段
     */
    public String insertField;

    /**
     * 上次更新完成时间
     */
    public Date updateTime;

    public TablePhyHomeDTO(BaseEntity entity) {
        super(entity);
    }

    public static <T extends BaseEntity> List<TablePhyHomeDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(TablePhyHomeDTO::new).collect(Collectors.toList());
    }
}
