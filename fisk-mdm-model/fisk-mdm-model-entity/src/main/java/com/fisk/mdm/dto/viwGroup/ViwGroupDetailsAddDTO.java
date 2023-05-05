package com.fisk.mdm.dto.viwGroup;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "组id")
    @NotNull
    private Integer groupId;
    @ApiModelProperty(value = "详细信息名称列表")
    private List<ViwGroupDetailsNameDTO> detailsNameList;
}
