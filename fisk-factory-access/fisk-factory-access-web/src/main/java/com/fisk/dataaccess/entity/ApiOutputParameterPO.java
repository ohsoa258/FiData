package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 * @date 2022-08-17 15:08
 */
@Data
@TableName("tb_api_output_parameter")
@EqualsAndHashCode(callSuper = true)
public class ApiOutputParameterPO extends BasePO {

    /**
     * 数据目标id
     */
    public Long dataTargetId;
    /**
     * 参数类型：header 或 body
     */
    public String parameterType;
    /**
     * form-data or raw
     */
    public String requestMethod;
    /**
     * 请求参数key
     */
    public String queryParamsKey;
    /**
     * 请求参数value
     */
    public String queryParamsValue;
    /**
     * 请求参数描述
     */
    public String queryParamsDescribe;

}
