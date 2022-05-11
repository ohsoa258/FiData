package com.fisk.mdm.dto.masterdata;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class MasterDataDTO extends MasterDataBaseDTO {

    public List<MemberDTO> members;

}
