package com.fisk.mdm.vo.resultObject;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author JianWenYang
 */
@Data
public class ResultAttributeGroupVO {

    private String name;

    private List<Map<String,Object>> resultData;

}
