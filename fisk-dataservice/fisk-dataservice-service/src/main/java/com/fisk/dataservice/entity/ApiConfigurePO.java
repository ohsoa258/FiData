package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
/**
 * @author WangYan
 * @date 2021/7/7 17:26
 */

@Data
@TableName("api_configure")
public class ApiConfigurePO extends BasePO {

    /**
     * 接口请求的表名
     */
    private String apiName;
    /**
     * 接口请求地址
     */
    private String apiRoute;
    /**
     * 是否去重(0:否  1:是)
     */
    private int distinctData;
    /**
     * 接口信息
     */
    private String apiInfo;
}
