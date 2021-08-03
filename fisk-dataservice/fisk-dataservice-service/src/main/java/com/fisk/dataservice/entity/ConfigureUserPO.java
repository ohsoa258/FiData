package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2021/7/23 11:14
 */

@Data
@TableName("configure_user")
public class ConfigureUserPO extends BasePO {

    /**
     * 下游系统名称
     */
    private String downSystemName;
    /**
     * 描述
     */
    private String systemInfo;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 密码
     */
    private String password;
}
