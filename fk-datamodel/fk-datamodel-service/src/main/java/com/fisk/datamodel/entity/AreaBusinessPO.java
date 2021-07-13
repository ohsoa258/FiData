package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 */
@Data
@TableName("tb_area_business")
@EqualsAndHashCode(callSuper = true)
public class AreaBusinessPO extends BasePO {

    /**
     * 业务域名称
     */
    public String businessName;

    /**
     * 业务域描述
     */
    public String businessDes;

    /**
     * 业务需求管理员
     */
    public String businessAdmin;

    /**
     * 应用负责人邮箱
     */
    public String businessEmail;

}
