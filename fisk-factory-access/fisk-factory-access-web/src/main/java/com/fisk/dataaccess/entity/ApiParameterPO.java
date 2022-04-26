package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * api请求参数表
 * </p>
 *
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-04-26 11:07:14
 */
@Data
@TableName("tb_api_parameter")
@EqualsAndHashCode(callSuper = true)
public class ApiParameterPO extends BasePO {

    /**
     * tb_api_config(id)
     */
    public long apiId;

    /**
     * 请求参数key
     */
    public String parameterKey;

    /**
     * 请求参数value
     */
    public String parameterValue;

}
