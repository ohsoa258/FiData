package com.fisk.system.dto.roleinfo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author JianWenYang
 */
@Data
public class RoleInfoDTO {

    @ApiModelProperty(value = "id")
    public long id;
    /**
     *角色名称
     */
    @ApiModelProperty(value = "角色名称")
    public String roleName;

    /**
     *角色描述
     */
    @ApiModelProperty(value = "角色描述")
    public String roleDesc;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;

    @ApiModelProperty(value = "创建者")
    public String createUser;
}
