package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_app_datasource")
public class AppDataSourcePO extends BaseEntity {
    /**
     * 主键
     */
    @TableId
    private long id;

    /**
     * tb_app_registration表id
     */
    private long appid;

    /**
     * tb_app_drivetype表type
     */
    private String driveType;

    /**
     * 数据源连接字符串
     */
    private String connectStr;

    /**
     * 连接账号
     */
    private String connectAccount;

    /**
     * 连接密码
     */
    private String connectPwd;

    /**
     * 验证方式（实时） 登录账号
     */
    private String realtimeAccount;

    /**
     * 验证方式（实时） 登录密码
     */
    private String realtimePwd;


    /**
     * 创建人(id)
     */
    private String createUser;

    /**
     * 更新人(id)
     */
    private String updateUser;

    /**
     * 逻辑删除(1: 未删除; 0: 删除)
     */
    private int delFlag;
}
