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
     * 配置集合
     */
    public List<ApiOutputParameterVO> parameters;

}
