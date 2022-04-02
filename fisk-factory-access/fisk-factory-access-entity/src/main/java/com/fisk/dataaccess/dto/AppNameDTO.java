package com.fisk.dataaccess.dto;

import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.entity.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(value = "应用名称", required = true)
    public String appName;

    @ApiModelProperty(value = "应用类型", required = true)
    public byte appType;
    @ApiModelProperty(value = "主键", required = true)
    public long id;

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
