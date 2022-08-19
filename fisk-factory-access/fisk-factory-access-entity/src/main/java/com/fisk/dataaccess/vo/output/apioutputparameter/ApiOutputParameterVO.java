package com.fisk.dataaccess.vo.output.apioutputparameter;

import lombok.Data;

/**
 * @author JianWenYang
 * @date 2022-08-18 15:20
 */
@Data
public class ApiOutputParameterVO {

    public Long id;
    /**
     * 数据目标id
     */
    public Long dataTargetId;
    /**
     * 参数类型：header 或 body
     */
    public String parameterType;
    /**
     * header请求key
     */
    public String queryParamsKey;
    /**
     * header请求value
     */
    public String queryParamsValue;

}
