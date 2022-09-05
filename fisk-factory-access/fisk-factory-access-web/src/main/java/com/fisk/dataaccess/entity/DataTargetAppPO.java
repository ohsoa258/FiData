package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_data_target_app")
@EqualsAndHashCode(callSuper = true)
public class DataTargetAppPO extends BasePO {

    /**
     * 数据目标应用名称
     */
    public String name;

    /**
     * 描述
     */
    public String description;

    /**
     * 负责人
     */
    public String principal;

    /**
     * 负责人邮箱
     */
    public String email;

}
