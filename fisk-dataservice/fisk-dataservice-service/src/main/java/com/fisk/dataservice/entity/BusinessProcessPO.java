package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/8/12 17:31
 */
@Data
@TableName("tb_business_process")
public class BusinessProcessPO extends BasePO {

    /**
     * 业务域id
     */
    private Integer businessId;
    /**
     * 业务过程名称
     */
    private String businessProcessCnName;
    /**
     * 业务过程英文名称
     */
    private String businessProcessEnName;
    /**
     * 业务过程描述
     */
    private String businessProcessDesc;
}
