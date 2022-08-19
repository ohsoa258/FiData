package com.fisk.dataaccess.dto.output.datatarget;

import com.fisk.dataaccess.dto.apioutputparameter.ApiOutputParameterDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 * @date 2022-08-18 14:17
 */
@Data
public class DataTargetAddDTO {

    public Integer id;
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
     * 主机端口
     */
    public Integer port;
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
     * 请求参数配置
     */
    public List<ApiOutputParameterDTO> parameters;

}
