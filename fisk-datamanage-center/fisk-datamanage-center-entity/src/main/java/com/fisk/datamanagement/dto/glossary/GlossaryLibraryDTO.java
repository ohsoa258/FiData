package com.fisk.datamanagement.dto.glossary;

import com.baomidou.mybatisplus.annotation.*;
import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @ClassName:
 * @Author: 湖~Tloml
 * @Date: 2023
 * @Copyright: 2023 by 湖~Tloml
 * @Description:
 **/
@Data
public class GlossaryLibraryDTO extends BaseDTO {

    public Integer pid;

    public String name;

    public String shortDescription;

    public String longDescription;
}
