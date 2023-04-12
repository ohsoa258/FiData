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
@TableName("tb_app_service_config")
public class AppServiceConfigPO extends BasePO {
    /**
     * 应用id
     */
    public int appId;

    /**
     * 服务id
     */
    public int serviceId;

    /**
     * API 状态 1启用、0禁用
     */
    public int apiState;

    /**
     * 类型：1api服务 2表服务 3 文件服务
     */
    public Integer type;
}
