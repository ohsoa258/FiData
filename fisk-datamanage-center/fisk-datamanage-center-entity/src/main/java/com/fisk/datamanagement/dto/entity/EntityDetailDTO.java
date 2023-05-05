package com.fisk.datamanagement.dto.entity;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class EntityDetailDTO {

    @ApiModelProperty(value = "实体详情Json")
    public JSONObject entityDetailJson;
}
