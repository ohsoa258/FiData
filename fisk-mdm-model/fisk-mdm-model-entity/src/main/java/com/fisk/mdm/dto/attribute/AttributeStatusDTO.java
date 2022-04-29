package com.fisk.mdm.dto.attribute;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @Author WangYan
 * @Date 2022/4/25 10:45
 * @Version 1.0
 */
@Data
public class AttributeStatusDTO {

    @NotNull
    @ApiModelProperty(value = "id",required = true)
    private Integer id;

    @ApiModelProperty(value = "底层表名")
    private String columnName;

    /**
     *状态： 0：新增 ，1：修改 ，2:已提交 3：删除
     */
    @ApiModelProperty(value = "状态")
    private Integer status;

    /**
     * 提交状态：0：提交失败 1：提交成功
     */
    @ApiModelProperty(value = "提交状态")
    private Integer syncStatus;

    @ApiModelProperty(value = "提交失败描述")
    //@Length(min = 0, max = 200, message = "长度最多200")
    private String errorMsg;
}
