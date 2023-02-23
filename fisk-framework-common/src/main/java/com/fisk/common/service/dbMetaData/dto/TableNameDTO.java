package com.fisk.common.service.dbMetaData.dto;

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
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TableNameDTO extends BaseDTO {

    /**
     * 表架构名称
     */
    @ApiModelProperty(value = "表架构名称")
    public String tableFramework;

    /**
     * 表名
     */
    @ApiModelProperty(value = "表名", required = true)
    public String tableName;

    /**
     * 表架构名称+表名
     */
    @ApiModelProperty(value = "表架构名称+表名", required = true)
    public String tableFullName;

    /**
     * 表行数
     */
    @ApiModelProperty(value = "表行数")
    public int rowsCount;

    /**
     * 返回给前端的唯一标记
     */
    @ApiModelProperty(value = "返回给前端的唯一标记", required = true)
    public int tag;

    /**
     * 表字段
     */
    public List<TableStructureDTO> fields;

    public TableNameDTO(BaseEntity entity) {
        super(entity);
    }

    public static <T extends BaseEntity> List<TableNameDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(TableNameDTO::new).collect(Collectors.toList());
    }

}
