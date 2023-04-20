package com.fisk.datamanagement.dto.businessclassification;

import com.baomidou.mybatisplus.annotation.*;
import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @ClassName:
 * @Author: 湖~Tloml
 * @Date: 2023
 * @Copyright: 2023 by 湖~Tloml
 * @Description:
 **/
@Data
public class BusinessClassificationDTO{

    @ApiModelProperty(value = "pid")
    public Integer pid;

    @ApiModelProperty(value = "名称")
    public String name;

    @ApiModelProperty(value = "描述")
    public String description;

    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    @ApiModelProperty(value = "创建者")
    public String createUser;

    @ApiModelProperty(value = "逻辑删除")
    public Boolean delFlag;
}
