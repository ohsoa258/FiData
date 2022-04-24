package com.fisk.mdm.dto.modelVersion;

import com.fisk.mdm.enums.ModelVersionStatusEnum;
import com.fisk.mdm.enums.ModelVersionTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author ChenYa
 */
@Data
public class ModelVersionDTO {


    @ApiModelProperty(value = "modelID")
    @NotNull()
    public int modelId;

    @ApiModelProperty(value = "model版本名称")
    @NotNull()
    @Length(min = 0, max = 50, message = "长度最多50")
    public String name;

    @ApiModelProperty(value = "model版本描述")
    @NotNull()
    @Length(min = 0, max = 200, message = "长度最多200")
    public String desc;

    @ApiModelProperty(value = "版本状态，0 打开、1 锁定、2 已提交")
    public Integer status;

    @ApiModelProperty(value = "版本类型，1 用户创建、2 自动创建")
    public Integer type;

}
