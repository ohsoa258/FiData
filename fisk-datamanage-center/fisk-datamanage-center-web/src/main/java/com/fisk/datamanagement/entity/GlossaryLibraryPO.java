package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @ClassName:
 * @Author: 湖~Tloml
 * @Date: 2023
 * @Copyright: 2023 by 湖~Tloml
 * @Description:
 **/
@Data
@TableName(value = "tb_glossary_library")
public class GlossaryLibraryPO extends BasePO {

    public Integer pid;

    public String name;

    public String shortDescription;

    public String longDescription;
}
