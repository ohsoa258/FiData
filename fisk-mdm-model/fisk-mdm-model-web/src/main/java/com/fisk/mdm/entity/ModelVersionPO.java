package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.mdm.enums.ModelVersionStatusEnum;
import com.fisk.mdm.enums.ModelVersionTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
@TableName("tb_model_version")
public class ModelVersionPO extends BasePO {

    /**
     * 模型ID
     */
    public int modelId;

    /**
     * 版本名称
     */
    public String name;

    /**
     * 版本描述
     */
    @TableField(value = "`desc`")
    public String desc;

    /**
     * 版本状态，0 打开、1 锁定、2 已提交
     */
    public ModelVersionStatusEnum status;

    /**
     * 版本类型，0 用户创建、1 自动创建
     */
    public ModelVersionTypeEnum type;
}
