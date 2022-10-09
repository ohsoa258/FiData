package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗，API清洗请求参数
 * @date 2022/10/8 15:33
 */
@Data
@TableName("tb_bizfilter_api_parm")
public class BusinessFilterApiParmPO extends BasePO {
    /**
     * tb_bizfilter_rule表主键ID
     */
    public String ruleId;

    /**
     * tb_bizfilter_api_config表主键ID
     */
    public int apiId;

    /**
     * api 参数类型 1：授权请求参数  2：正文请求参数
     */
    public int apiParmType;

    /**
     * 参数正文类型 formData/row
     */
    public String apiParmBodyType;

    /**
     * 参数key
     * */
    public String apiParmKey;

    /**
     * 参数value
     * */
    public String apiParmValue;

    /**
     * 参数value标识
     * */
    public String apiParmValueUnique;
}
