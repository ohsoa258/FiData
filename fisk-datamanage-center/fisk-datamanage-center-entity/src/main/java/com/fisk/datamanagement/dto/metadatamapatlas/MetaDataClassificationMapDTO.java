package com.fisk.datamanagement.dto.metadatamapatlas;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.dto.BaseDTO;
import com.fisk.common.core.baseObject.entity.BasePO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName:
 * @Author: 湖~Tloml
 * @Date: 2023
 * @Copyright: 2023 by 湖~Tloml
 * @Description:
 **/

@Data
public class MetaDataClassificationMapDTO extends BaseDTO {

    @ApiModelProperty(value = "元数据实体ID")
    public Integer metaDataEntityId;

    @ApiModelProperty(value = "业务分类ID")
    public Integer businessClassificationId;
}
