package com.fisk.mdm.vo.resultObject;

import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 结果对象
 *
 * @author ChenYa
 * @date 2022/04/27
 */
@Data
public class ResultObjectVO {
    private List<AttributeInfoDTO> attributeInfoDTOList;
    private List<Map<String,Object>> resultData;
}
