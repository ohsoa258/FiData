package com.fisk.dataaccess.vo.output.datatarget;

import com.fisk.dataaccess.vo.output.apioutputparameter.ApiOutputParameterVO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 * @date 2022-08-18 15:09
 */
@Data
public class DataTargetVO {

    public Long id;
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
     * 请求方式：1:Get 2:Post
     */
    public Integer requestWay;
    /**
     * 主机地址
     */
    public String host;
    /**
     * ip请求接口
     */
    public String apiAddress;
    /**
     * 数据目标类型
     */
    public Integer type;
    /**
     * 连接账号
     */
    public String connectAccount;
    /**
     * 连接面
     */
    public String connectPwd;
    /**
     * 请求方法：form-data or raw
     */
    public String requestMethod;
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
     * 配置集合
     */
    public List<ApiOutputParameterVO> parameters;

}
