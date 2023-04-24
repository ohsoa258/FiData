package com.fisk.chartvisual.dto.dstable;

import com.fisk.chartvisual.enums.isExistTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/3/8 15:35
 */
@Data
public class FieldInfoDTO {

    /**
     * 唯一标识
     */
    @ApiModelProperty(value = "id")
    private Integer id;
    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    private String field;
    /**
     * 目标字段名称
     */
    @ApiModelProperty(value = "目标字段")
    private String targetField;
    /**
     * 字段类型
     */
    @ApiModelProperty(value = "字段类型")
    private String type;
    /**
     * 目标字段类型
     */
    @ApiModelProperty(value = "目标字段类型")
    private String targetType;
    /**
     * 字段描述
     */
    @ApiModelProperty(value = "字段信息")
    private String fieldInfo;
    /**
     * 表是否存在库里
     */
    @ApiModelProperty(value = "表是否存在库里")
    private isExistTypeEnum fieldIsExist;
}
