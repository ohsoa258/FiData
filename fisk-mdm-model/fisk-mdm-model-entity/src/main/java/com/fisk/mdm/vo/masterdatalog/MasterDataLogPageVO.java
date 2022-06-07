package com.fisk.mdm.vo.masterdatalog;

import com.fisk.mdm.vo.attribute.AttributeColumnVO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author JianWenYang
 */
@Data
public class MasterDataLogPageVO {

    private Integer total;

    private List<Map<String, Object>> resultData;

    private List<AttributeColumnVO> attributes;

}
