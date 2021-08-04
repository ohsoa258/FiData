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
public class AppNameDTO extends BaseDTO {

    public String appName;

    public byte appType;

    public long id;

//    public List<String> appName;

    public AppNameDTO(BaseEntity entity) {
        super(entity);
    }

    public static <T extends BaseEntity> List<AppNameDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(AppNameDTO::new).collect(Collectors.toList());
    }

}
