package com.fisk.mdm.dto.viwGroup;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/5/24 15:41
 * @Version 1.0
 */
@Data
public class ViwGroupDetailsAddDTO {

    private Integer id;
    @NotNull
    private Integer groupId;
    private List<ViwGroupDetailsNameDTO> detailsNameList;
}
