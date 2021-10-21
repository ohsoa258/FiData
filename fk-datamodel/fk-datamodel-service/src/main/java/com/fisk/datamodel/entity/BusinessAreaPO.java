package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author Lock
 */
@Data
@TableName("tb_area_business")
@EqualsAndHashCode(callSuper = true)
public class BusinessAreaPO extends BasePO {

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

    /**
     * 发布状态：1:未发布、2：发布成功、3：发布失败
     */
    private int isPublish;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

}
