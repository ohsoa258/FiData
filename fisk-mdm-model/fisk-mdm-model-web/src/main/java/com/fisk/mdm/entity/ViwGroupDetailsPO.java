package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/5/24 15:24
 * @Version 1.0
 */
@TableName("tb_viw_group_details")
@Data
public class ViwGroupDetailsPO extends BasePO {

    private Integer groupId;
    private Integer attributeId;
    private String aliasName;
}
