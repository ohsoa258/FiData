package com.fisk.datamanagement.dto.lineage;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class LineAgeDTO {

    @ApiModelProperty(value = "guid实体图")
    public List<JSONObject> guidEntityMap;

    @ApiModelProperty(value = "关系")
    public List<LineAgeRelationsDTO> relations;

}
