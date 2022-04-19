package com.fisk.mdm.vo.entity;

import com.fisk.mdm.dto.attribute.AttributeDTO;
import com.fisk.mdm.enums.MdmStatusTypeEnum;
import com.fisk.mdm.enums.WhetherTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/4/18 22:44
 * @Version 1.0
 */
@Data
public class EntityVO {

    private Integer id;
    private Integer modelId;
    private String name;
    private String displayName;
    private String desc;
    private MdmStatusTypeEnum status;
    private String tableName;
    private WhetherTypeEnum enableMemberLog;
    private Integer approvalRuleId;
    private Integer buildCodeRuleId;
    private Integer hierarchyId;
    /**
     * 属性信息
     */
    private List<AttributeDTO> attributeList;
}
