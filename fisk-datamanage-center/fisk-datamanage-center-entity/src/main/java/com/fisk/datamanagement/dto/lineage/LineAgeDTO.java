package com.fisk.datamanagement.dto.lineage;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class LineAgeDTO {

    public List<JSONObject> guidEntityMap;

    public List<LineAgeRelationsDTO> relations;

}
