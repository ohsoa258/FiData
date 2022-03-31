package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 组件通知关联表
 * @date 2022/3/22 15:20
 */
@Data
@TableName("tb_component_notification")
public class ComponentNotificationPO extends BasePO {
    /**
     * 组件id
     */
    public int moduleId;

    /**
     * 通知id
     */
    public int noticeId;

    /**
     * 模板id
     */
    public  int templateId;
}
