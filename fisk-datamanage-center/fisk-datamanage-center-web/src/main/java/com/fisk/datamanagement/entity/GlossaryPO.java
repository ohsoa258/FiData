package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName:
 * @Author: 湖~Tloml
 * @Date: 2023
 * @Copyright: 2023 by 湖~Tloml
 * @Description:
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "tb_glossary")
public class GlossaryPO extends BasePO {

    public Integer glossaryLibraryId;

    public String name;

    public String shortDescription;

    public String longDescription;
}
