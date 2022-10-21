package com.fisk.dataaccess.dto.app;

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
public class AppDriveTypeDTO extends BaseDTO {

    /**
     * id
     */
    @ApiModelProperty(value = "主键")
    public long id;

    /**
     * 驱动类型
     */
    @ApiModelProperty(value = "驱动类型", required = true)
    public String name;

    /**
     * 模板
     */
    @ApiModelProperty(value = "连接模板", required = true)
    public String connectStr;

    @ApiModelProperty(value = "类型: 0数据库 1非数据库", required = true)
    public Integer type;


    public AppDriveTypeDTO(BaseEntity entity) {
        super(entity);
    }

    /**
     * 将PO集合转为DTO对象
     *
     * @param list PO对象集合
     * @param <T>  PO的类型
     * @return DTO集合
     */
    public static <T extends BaseEntity> List<AppDriveTypeDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(AppDriveTypeDTO::new).collect(Collectors.toList());
    }

}
