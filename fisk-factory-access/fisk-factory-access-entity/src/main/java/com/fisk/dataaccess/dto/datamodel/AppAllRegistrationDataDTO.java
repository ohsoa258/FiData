package com.fisk.dataaccess.dto.datamodel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author SongJianJian
 */
@Data
public class AppAllRegistrationDataDTO {

    @ApiModelProperty(value = "id")
    private long id;

    @ApiModelProperty(value = "name")
    private String name;

    @ApiModelProperty(value = "app列表")
    public List<AppRegistrationDataDTO> appList = new ArrayList<>();

}
