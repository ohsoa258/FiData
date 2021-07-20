package com.fisk.dataservice.dto;

import com.fisk.dataservice.entity.ApiConfigureFieldPO;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/7/20 15:21
 */
@Data
public class ApiFieldDataDTO {
    public String apiName;
    public String apiInfo;
    public String tableName;
    public List<ApiConfigureFieldPO> apiConfigureFieldList;
}
