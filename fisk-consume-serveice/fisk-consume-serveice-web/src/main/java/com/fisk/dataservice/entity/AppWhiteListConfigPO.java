package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 应用白名单配置
 * @date 2023/6/7 9:33
 */
@Data
@TableName("tb_app_whitelist_config")
public class AppWhiteListConfigPO extends BasePO {
    /**
     * 客户端ip
     */
    public String clientIp;

    /**
     * 用户名称
     */
    public String userName;
}
