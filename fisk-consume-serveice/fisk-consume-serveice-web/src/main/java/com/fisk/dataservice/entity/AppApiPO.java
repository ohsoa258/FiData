package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description 应用api实体类
 * @date 2022/1/6 14:51
 */
@Data
@TableName("tb_app_api")
public class AppApiPO extends BasePO{
    /**
     * 应用id
     */
    public int appId;

    /**
     * API id
     */
    public int apiId;

    /**
     * API 状态 1启用、0禁用
     */
    public int apiState;
}
