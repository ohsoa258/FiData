package com.fisk.datamodel.dto.derivedindicator;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author JianWenYang
 */
@Data
public class DerivedIndicatorsAddDTO extends DerivedIndicatorsDTO {
    public Date createTime;
    public String createUser;
}
