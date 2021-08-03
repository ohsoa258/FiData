package com.fisk.dataservice.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author WangYan
 * @date 2021/8/3 16:51
 */
@Data
public class ApiConfigureDTO {
    @NotNull(message = "id不可为null")
    public Integer id;
    private String apiName;
    private String apiRoute;
    private String tableName;
    private String apiInfo;
}
