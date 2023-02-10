package com.fisk.datamanagement.dto.businessclassification;

import com.baomidou.mybatisplus.annotation.*;
import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.entity.BasePO;
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
@TableName("tb_business_classification")
public class BusinessClassificationDTO extends BasePO {

    public Integer pid;

    public String name;

    public String description;
}
