package com.fisk.mdm.vo.resultObject;

import com.fisk.mdm.vo.attribute.AttributeColumnVO;
import lombok.Data;

import java.util.List;

/**
 * 结果对象
 *
 * @author ChenYa
 * @date 2022/04/27
 */
@Data
public class ResultObjectVO {
    private List<AttributeColumnVO> attributeColumnVoList;
    private List<ResultAttributeGroupVO> attributeGroups;
}
