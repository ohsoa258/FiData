package com.fisk.mdm.vo.resultObject;

import com.fisk.mdm.vo.attribute.AttributeColumnVO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author JianWenYang
 */
@Data
public class ResultAttributeGroupVO {

    private String name;

    private List<AttributeColumnVO> attributes;

}
