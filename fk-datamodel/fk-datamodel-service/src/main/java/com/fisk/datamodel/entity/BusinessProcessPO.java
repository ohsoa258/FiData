package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@TableName("tb_business_process")
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessProcessPO extends BasePO {
    /**
     * 业务域id
     */
    public int businessId;
    /**
     * 业务过程名称
     */
    public String businessProcessCnName;
    /**
     * 业务过程英文名称
     */
    public String businessProcessEnName;
    /**
     * 业务过程描述
     */
    public String businessProcessDesc;
    /**
     * 是否发布
     */
    public Boolean isPublish;

}
