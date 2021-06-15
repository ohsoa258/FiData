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
 * @author: Lock
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TableAppNameDTO extends BaseDTO {

    public String appName;

    public TableAppNameDTO(BaseEntity entity) {
        super(entity);
    }

    public static <T extends BaseEntity> List<TableAppNameDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(TableAppNameDTO::new).collect(Collectors.toList());
    }

}
