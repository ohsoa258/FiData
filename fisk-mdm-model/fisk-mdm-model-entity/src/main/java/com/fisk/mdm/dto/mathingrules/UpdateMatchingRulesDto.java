package com.fisk.mdm.dto.mathingrules;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JinXingWang
 */
@Data
public class UpdateMatchingRulesDto extends MatchingRulesDto{
    @ApiModelProperty(value = "id")
    public long id;
}
