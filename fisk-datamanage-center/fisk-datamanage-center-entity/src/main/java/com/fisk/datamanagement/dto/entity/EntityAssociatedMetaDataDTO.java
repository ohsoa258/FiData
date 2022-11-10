package com.fisk.datamanagement.dto.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class EntityAssociatedMetaDataDTO {

    public String guid;

    public JSONObject businessMetaDataAttribute;

}
