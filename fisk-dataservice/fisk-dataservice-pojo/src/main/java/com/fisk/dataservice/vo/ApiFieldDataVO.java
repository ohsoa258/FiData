package com.fisk.dataservice.vo;

import com.fisk.dataservice.dto.ApiConfigureField;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2021/7/20 15:21
 */
@Data
public class ApiFieldDataVO {
    public String apiName;
    public String apiInfo;
    public String tableName;
    public List<ApiConfigureField> apiConfigureFieldList;
}
