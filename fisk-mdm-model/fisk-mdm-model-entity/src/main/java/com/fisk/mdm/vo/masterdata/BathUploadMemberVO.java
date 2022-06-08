package com.fisk.mdm.vo.masterdata;

import com.fisk.mdm.dto.attribute.AttributeInfoDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author JianWenYang
 */
@Data
public class BathUploadMemberVO {

    private Integer addCount;

    private Integer count;

    private Integer entityId;

    private String entityName;

    private Integer errorCount;

    private Integer successCount;

    private Integer updateCount;

    private Integer submitSuccessCount;

    private Integer submitErrorCount;

    private Integer versionId;

    private List<AttributeInfoDTO> attribute;

    private List<Map<String, Object>> members;

}
