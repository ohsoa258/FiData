package com.fisk.mdm.vo.modelVersion;

import com.alibaba.fastjson.annotation.JSONField;
import com.fisk.mdm.enums.ModelVersionStatusEnum;
import com.fisk.mdm.enums.ModelVersionTypeEnum;
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
public class ModelVersionVO {
    @ApiModelProperty(value = "主键")
    public int id;

    @ApiModelProperty(value = "modelID")
    public int modelId;

    @ApiModelProperty(value = "model版本名称")
    public String name;

    @ApiModelProperty(value = "model版本描述")
    public String desc;

    @ApiModelProperty(value = "版本状态，0 打开、1 锁定、2 已提交")
    public String status;

    @ApiModelProperty(value = "版本类型，1 用户创建、2 自动创建")
    public String type;

    @ApiModelProperty(value = "创建人")
    public String createUser;

    @ApiModelProperty(value = "创建时间")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime createTime;

    @ApiModelProperty(value = "更新人")
    public String updateUser;

    @ApiModelProperty(value = "更新时间")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime updateTime;
}
