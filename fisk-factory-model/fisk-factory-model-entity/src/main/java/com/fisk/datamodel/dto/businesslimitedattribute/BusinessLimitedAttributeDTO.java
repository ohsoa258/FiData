package com.fisk.datamodel.dto.businesslimitedattribute;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
/**
 * @author cfk
 */
@Data
public class BusinessLimitedAttributeDTO {
    @ApiModelProperty(value = "id")
    public int id;
    /**
     * 业务限定id
     */
    @ApiModelProperty(value = "业务限制Id")
    public int businessLimitedId;
    /**
     *事实字段表id
     */
    @ApiModelProperty(value = "事实属性Id")
    public int factAttributeId;
    /**
     *计算逻辑
     */
    @ApiModelProperty(value = "计算逻辑")
    public String calculationLogic;
    /**
     *计算值
     */
    @ApiModelProperty(value = "计算值")
    public String calculationValue;
    /**
     *创建时间
     */
    @ApiModelProperty(value = "创建时间")
    public Date createTime;
    /**
     *创建人
     */
    @ApiModelProperty(value = "创建者")
    public String createUser;
    /**
     *修改时间
     */
    @ApiModelProperty(value = "更新时间")
    public String updateTime;
    /**
     *更新人
     */
    @ApiModelProperty(value = "更新者")
    public String updateUser;
    /**
     *是否删除
     */
    @ApiModelProperty(value = "是否删除")
    public int delFlag;
}
