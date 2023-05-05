package com.fisk.datamodel.dto.businesslimited;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
/**
 * @author cfk
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessLimitedDTO {
    @ApiModelProperty(value = "id")
    public int id;
    /**
     * 业务限定名称
     */
    @ApiModelProperty(value = "业务限定名称")
    public String limitedName;
    /**
     * 业务限定描述
     */
    @ApiModelProperty(value = "业务限定描述")
    public String limitedDes;
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    public Date createTime;
    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    public String createUser;
    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    public Date updateTime;
    /**
     * 修改人
     */
    @ApiModelProperty(value = "修改人")
    public String updateUser;
    /**
     *是否删除
     */
    @ApiModelProperty(value = "是否删除")
    public int delFlag;
    /**
     * 事实表id
     */
    @ApiModelProperty(value = "事实表id")
    public int factId;
    /**
     * 业务过程
     */
    @ApiModelProperty(value = "业务过程")
    public String businessProcessCnName;
    /**
     * 业务域
     */
    @ApiModelProperty(value = "业务域")
    public String businessName;
}
