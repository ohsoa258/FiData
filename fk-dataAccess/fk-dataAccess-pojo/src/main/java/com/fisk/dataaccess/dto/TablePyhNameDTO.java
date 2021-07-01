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
 * <p>
 * 表名及表对应字段
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TablePyhNameDTO extends BaseDTO {

    /**
     * 非实时应用所属下的表名
     */
    public String tableName;

    /**
     * 返回给前端的唯一标记
     */
    public int tag;

    /**
     * 表字段
     */
    public List<String> fields;

    public TablePyhNameDTO(BaseEntity entity) {
        super(entity);
    }

    public static <T extends BaseEntity> List<TablePyhNameDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(TablePyhNameDTO::new).collect(Collectors.toList());
    }

}
