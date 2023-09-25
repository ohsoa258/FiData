package com.fisk.task.dto.task;

import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.enums.OlapTableEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author cfk
 */
@Data
public class BuildDeleteTableApiServiceDTO extends MQBaseDTO {

    /**
     * 应用Id
     */
    @ApiModelProperty(value = "应用Id")
    public String appId;
    /**
     * 表类别
     */
    @ApiModelProperty(value = "表类别")
    public OlapTableEnum olapTableEnum;

    /**
     * 是否删除业务域/应用/模型
     */
    @ApiModelProperty(value = "是否删除业务域/应用/模型")
    public boolean delBusiness;
    /**
     * 表id集合
     */
    @ApiModelProperty(value = "apiId集合")
    public List<Long> ids;
}
