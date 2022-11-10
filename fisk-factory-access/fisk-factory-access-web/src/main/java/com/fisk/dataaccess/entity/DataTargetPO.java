package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 * @date 2022-08-17 14:53
 */
@Data
@TableName("tb_data_target")
@EqualsAndHashCode(callSuper = true)
public class DataTargetPO extends BasePO {

    /**
     * 数据目标应用id
     */
    public Integer dataTargetAppId;
    /**
     * 1:Oracle 2:SqlServer 3:MySql 4:FTP 5:API
     */
    public Integer type;
    /**
     * 名称
     */
    public String name;
    /**
     * 负责人
     */
    public String principal;
    /**
     * 描述
     */
    public String description;
    /**
     * 主机
     */
    public String host;
    /**
     * 端口
     */
    public Integer port;
    /**
     * 连接账号
     */
    public String connectAccount;
    /**
     * 连接密码
     */
    public String connectPwd;
    /**
     * 身份验证方式: 0: 空; 1: OAuth 1.0; 2: OAuth 1.0;3: JWT;  4: Bearer Token;  5:无需身份验证
     */
    public Integer authenticationMethod;
    /**
     * 身份验证url
     */
    public String authenticationUrl;
    /**
     * Bearer Token验证方式的Token
     */
    public String token;
    /**
     * 请求方式
     */
    public Integer requestWay;
    /**
     * 请求方式：1:Get 2:Post
     */
    public String apiAddress;

}
