package com.fisk.datamodel.dto.businesslimitedattribute;

import lombok.Data;

import java.util.Date;
/**
 * @author cfk
 */
@Data
public class BusinessLimitedAttributeDTO {
    public int id;
    //业务限定id
    public int businessLimitedId;
    //事实字段表id
    public int factAttributeId;
    //计算逻辑
    public String calculationLogic;
    //计算值
    public String calculationValue;
    //创建时间
    public Date createTime;
    //创建人
    public String createUser;
    //修改时间
    public String updateTime;
    //更新人
    public String updateUser;
    //是否删除
    public int delFlag;
}
