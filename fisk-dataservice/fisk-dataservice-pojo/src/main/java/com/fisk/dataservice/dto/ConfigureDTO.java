package com.fisk.dataservice.dto;

import lombok.Data;

/**
 * @author WangYan
 * @date 2021/8/31 16:31
 */
@Data
public class ConfigureDTO {

    public Integer id;
    public String apiName;
    public String apiRoute;
    public String tableName;
    public String apiInfo;
    /**
     * 0 未选中  1选中
     */
    public Integer check;
}
