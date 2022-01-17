package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * api配置表
 * </p>
 *
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-01-17 14:45:02
 */
@Data
@TableName("tb_api_config")
@EqualsAndHashCode(callSuper = true)
public class ApiConfigPO extends BasePO {

    /**
     * api名称
     */
    public String apiName;

    /**
     * api描述
     */
    public String apiDes;

}
