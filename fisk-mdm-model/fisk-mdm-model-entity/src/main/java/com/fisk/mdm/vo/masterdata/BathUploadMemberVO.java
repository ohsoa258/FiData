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

    public int addCount;

    public int count;

    public int entityId;

    public String entityName;

    public int errorCount;

    public int successCount;

    public int updateCount;

    public int submitSuccessCount;

    public int submitErrorCount;

    public int versionId;

    public List<AttributeInfoDTO> attribute;

    public List<Map<String, Object>> members;

}
