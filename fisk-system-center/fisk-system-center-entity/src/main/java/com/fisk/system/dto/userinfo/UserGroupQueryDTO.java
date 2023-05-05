package com.fisk.system.dto.userinfo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class UserGroupQueryDTO {

    @ApiModelProperty(value = "用户id列表")
    public List<Integer> userIdList;

    @ApiModelProperty(value = "分页")
    public int page;

    @ApiModelProperty(value = "大小")
    public int size;

    /**
     *角查询字段名称
     */
    @ApiModelProperty(value = "角查询字段名称")
    public String name;


}
