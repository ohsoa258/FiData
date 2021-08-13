package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/8/12 10:23
 */
@Data
@TableName("tb_area_business")
public class AreaBusinessPO extends BasePO {

    /**
     * 业务域名称
     */
    private String businessName;
    /**
     * 业务域描述
     */
    private String businessDes;
    /**
     * 业务需求管理员
     */
    private String businessAdmin;
    /**
     * 应用负责人邮箱
     */
    private String businessEmail;
}
