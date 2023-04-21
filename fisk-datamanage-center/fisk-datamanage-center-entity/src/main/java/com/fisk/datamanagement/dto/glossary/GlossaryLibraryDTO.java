package com.fisk.datamanagement.dto.glossary;

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
@EqualsAndHashCode(callSuper = true)
@Data
public class GlossaryLibraryDTO extends BaseDTO {

    @ApiModelProperty(value = "pid")
    public Integer pid;

    @ApiModelProperty(value = "名称")
    public String name;

    @ApiModelProperty(value = "简短的描述")
    public String shortDescription;

    @ApiModelProperty(value = "详细的描述")
    public String longDescription;
}
