package com.fisk.datamodel.dto.derivedindicator;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author JianWenYang
 */
@Data
public class DerivedIndicatorsAddDTO extends DerivedIndicatorsDTO {
    @ApiModelProperty(value = "创建时间")
    public Date createTime;
    @ApiModelProperty(value = "创建者")
    public String createUser;
}
