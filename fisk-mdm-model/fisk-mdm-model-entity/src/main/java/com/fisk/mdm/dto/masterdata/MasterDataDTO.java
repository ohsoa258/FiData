package com.fisk.mdm.dto.masterdata;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class MasterDataDTO {

    public int entityId;

    public int versionId;

    public int modelId;

    public List<MemberDTO> members;

}
