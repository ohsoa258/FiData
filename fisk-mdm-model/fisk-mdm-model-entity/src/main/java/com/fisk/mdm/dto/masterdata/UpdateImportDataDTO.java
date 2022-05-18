package com.fisk.mdm.dto.masterdata;

import lombok.Data;

import java.util.Map;

/**
 * @author JianWenYang
 */
@Data
public class UpdateImportDataDTO {

    private Integer entityId;

    private Map<String, Object> data;

}
