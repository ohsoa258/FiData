package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/7/23 11:14
 */

@Data
@TableName("middle_configure")
public class MiddleConfigurePO extends BasePO {
    /**
     * 用户表id
     */
    private Integer userId;
    /**
     * 服务表id
     */
    private Integer configureId;
}
