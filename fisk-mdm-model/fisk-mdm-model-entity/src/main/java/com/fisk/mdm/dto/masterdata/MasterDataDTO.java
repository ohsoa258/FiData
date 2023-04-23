package com.fisk.mdm.dto.masterdata;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author JianWenYang
 */
@Data
public class MasterDataDTO extends MasterDataBaseDTO {

    private String description;
    private String fidataId;

    private List<Map<String, Object>> members;

}
