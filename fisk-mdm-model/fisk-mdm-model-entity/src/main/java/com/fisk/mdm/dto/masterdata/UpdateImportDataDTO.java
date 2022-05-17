package com.fisk.mdm.dto.masterdata;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class UpdateImportDataDTO {

    private Integer entityId;

    private JSONObject jsonObject;

}
