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
 * <p>
 * 实时对象
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TableFieldsDTO extends BaseDTO {

    /**
     * 主键id
     */
    @ApiModelProperty(value = "表字段主键id")
    public long id;

    /**
     * 功能类型
     * 0:旧数据不操作  1:新增  2:编辑
     */
    @ApiModelProperty(value = "0:旧数据不操作  1:新增  2:编辑", required = true)
    public int funcType;

    /**
     * table_access（id）
     */
    @ApiModelProperty(value = "物理表id", required = true)
    public Long tableAccessId;
    /**
     * 源字段
     */
    @ApiModelProperty(value = "源字段", required = true)
    public String sourceFieldName;

    @ApiModelProperty(value = "源字段类型", required = true)
    public String sourceFieldType;

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称", required = true)
    public String fieldName;

    /**
     * 字段描述
     */
    @ApiModelProperty(value = "字段描述", required = true)
    public String fieldDes;

    /**
     * 字段类型
     */
    @ApiModelProperty(value = "字段类型", required = true)
    public String fieldType;

    /**
     * 字段长度
     */
    @ApiModelProperty(value = "字段长度", required = true)
    public Long fieldLength;

    @ApiModelProperty(value = "字段推送规则")
    public String fieldPushRule;

    @ApiModelProperty(value = "字段推送示例")
    public String fieldPushExample;

    /**
     * 1是主键，0非主键
     */
    @ApiModelProperty(value = "1是主键，0非主键", required = true)
    public Integer isPrimarykey;

    /**
     * 1是业务时间，0非业务时间
     */
    @ApiModelProperty(value = "1是业务时间，0非业务时间", required = true)
    public int isBusinesstime;

    /**
     * 0是实时物理表，1是非实时物理表
     */
    @ApiModelProperty(value = "0是实时物理表，1是非实时物理表", required = true)
    public Integer isRealtime;

    /**
     * 1是时间戳，0非时间戳
     */
    @ApiModelProperty(value = "1是时间戳，0非时间戳", required = true)
    public int isTimestamp;

    /**
     * 应用简写
     */
    @ApiModelProperty(value = "应用简写")
    public String appbAbreviation;
    /**
     * 源表名
     */
    @ApiModelProperty(value = "源表名")
    public String originalTableName;

    public TableFieldsDTO(BaseEntity entity) {
        super(entity);
    }


    /**
     * 将PO集合转为DTO对象
     *
     * @param list PO对象集合
     * @param <T>  PO的类型
     * @return DTO集合
     */
    public static <T extends BaseEntity> List<TableFieldsDTO> convertEntityList(Collection<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().map(TableFieldsDTO::new).collect(Collectors.toList());
    }

}
