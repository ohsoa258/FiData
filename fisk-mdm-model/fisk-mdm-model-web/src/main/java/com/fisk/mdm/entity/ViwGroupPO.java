package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/5/24 15:22
 * @Version 1.0
 */
@TableName("tb_viw_group")
@Data
public class ViwGroupPO extends BasePO {

    private Integer entityId;
    private String name;
    private String details;
}
