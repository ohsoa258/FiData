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

    private String apiName;
    /**
     * 接口请求地址
     */
    private String apiRoute;
    /**
     * 接口请求的表名
     */
    private String tableName;
    /**
     * 接口信息
     */
    private String apiInfo;
}
