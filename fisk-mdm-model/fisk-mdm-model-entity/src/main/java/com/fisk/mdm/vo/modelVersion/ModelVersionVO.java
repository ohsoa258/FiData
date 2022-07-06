package com.fisk.mdm.vo.modelVersion;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.common.core.baseObject.vo.BaseUserInfoVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模型版本vo
 *
 * @author ChenYa
 * @date 2022/04/24
 */
@Data
public class ModelVersionVO extends BaseUserInfoVO {
    @ApiModelProperty(value = "主键")
    public int id;

    @ApiModelProperty(value = "modelID")
    public int modelId;

    @ApiModelProperty(value = "model版本名称")
    public String name;

    @ApiModelProperty(value = "model版本描述")
    public String desc;

    @ApiModelProperty(value = "版本状态，0 打开、1 锁定、2 发布")
    public Integer status;

    @ApiModelProperty(value = "版本类型，1 用户创建、2 自动创建")
    public Integer type;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime updateTime;
}
