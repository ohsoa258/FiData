package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
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

    public Long appId;
    /**
     * api名称
     */
    public String apiName;

    /**
     * api地址
     */
    public String apiAddress;

    /**
     * api请求方式(0: 空;  1: get;  2:post)
     */
    public Integer apiRequestType;

    /**
     * api描述
     */
    public String apiDes;

    /**
     * 0: 未发布  1: 发布成功  2: 发布失败  3: 正在发布
     */
    public Integer publish;
}
