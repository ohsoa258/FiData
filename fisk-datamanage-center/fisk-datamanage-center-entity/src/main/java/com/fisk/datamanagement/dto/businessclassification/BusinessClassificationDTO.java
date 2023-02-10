package com.fisk.datamanagement.dto.businessclassification;

import com.baomidou.mybatisplus.annotation.*;
import com.fisk.common.core.baseObject.dto.BaseDTO;
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
public class BusinessClassificationDTO extends BaseDTO {

    public String id;

    public String pid;

    public String name;

    public String description;

    public LocalDateTime createTime;

    public String createUser;

    public LocalDateTime updateTime;

    public String updateUser;
}
