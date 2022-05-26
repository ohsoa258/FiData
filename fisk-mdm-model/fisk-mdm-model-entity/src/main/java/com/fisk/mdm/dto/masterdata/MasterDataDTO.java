package com.fisk.mdm.dto.masterdata;

import lombok.Data;

import java.util.Map;

/**
 * @author JianWenYang
 */
@Data
public class MasterDataDTO extends MasterDataBaseDTO {

    private Map<String, Object> members;

}
