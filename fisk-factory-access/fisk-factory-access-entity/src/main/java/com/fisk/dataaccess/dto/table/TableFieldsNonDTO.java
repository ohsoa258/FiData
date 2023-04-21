package com.fisk.dataaccess.dto.table;

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
 *
 * 非实时对象
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TableFieldsNonDTO extends BaseDTO {

    /**
     * table_access（id）
     */
    @ApiModelProperty(value = "目标表ID")
    public long tableAccessId;
    /**
     * 源字段
     */
    @ApiModelProperty(value = "字段资源名称")
    public String sourceFieldName;

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    public String fieldName;

    /**
     * 字段描述
     */
    @ApiModelProperty(value = "字段描述")
    public String fieldDes;

    /**
     * 字段类型
     */
    @ApiModelProperty(value = "字段类型")
    public String fieldType;

    /**
     * 字段长度
     */
    @ApiModelProperty(value = "字段长度")
    public long fieldLength;

    @ApiModelProperty(value = "字段推送规则")
    public String fieldPushRule;

    @ApiModelProperty(value = "字段推送示例")
    public String fieldPushExample;

    /**
     * 1是主键，0非主键
     */
    @ApiModelProperty(value = "1是主键，0非主键")
    public int isPrimarykey;

    /**
     * 1是业务时间，0非业务时间
     */
    @ApiModelProperty(value = "1是业务时间，0非业务时间")
    public int isBusinesstime;

    /**
     * 1是时间戳，0非时间戳
     */
    @ApiModelProperty(value = "1是时间戳，0非时间戳")
    public long isRealtime;

    public TableFieldsNonDTO(BaseEntity entity) {
        super(entity);
    }

    /**
     * 将PO集合转为DTO对象
     *
     * @param list PO对象集合
     * @param <T>  PO的类型
     * @return DTO集合
     */
    public static <T extends BaseEntity> List<TableFieldsNonDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(TableFieldsNonDTO::new).collect(Collectors.toList());
    }

}
