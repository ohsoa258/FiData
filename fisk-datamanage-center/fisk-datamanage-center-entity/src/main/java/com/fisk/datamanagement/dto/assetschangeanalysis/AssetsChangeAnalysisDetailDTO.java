package com.fisk.datamanagement.dto.assetschangeanalysis;

import com.fisk.datamanagement.enums.EntityTypeEnum;
import com.fisk.datamanagement.enums.MetadataAuditOperationTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AssetsChangeAnalysisDetailDTO {

    /**
     * 审计明细id tb_metadata_entity_audit_atrribute_change audit_id
     */
    private Integer auditId;


    private Integer entityId;

    /**
     * 元数据类型编码
     */
    @ApiModelProperty(value = "元数据类型编码")
    private EntityTypeEnum type;

    /**
     * 元数据类型名称
     */
    @ApiModelProperty(value = "元数据类型名称")
    private String typeName;

    /**
     * 元数据名称
     */
    @ApiModelProperty(value = "元数据名称")
    private String entityName;

    /**
     * 变更类型
     */
    @ApiModelProperty(value = "变更类型 0全部 1 新增 2 修改 3 删除")
    private MetadataAuditOperationTypeEnum entityType;

    /**
     * 变更类型 名称
     */
    @ApiModelProperty(value = "变更类型 0全部 1 新增 2 修改 3 删除")
    private String entityTypeName;

    /**
     * 父级别元数据名称
     */
    @ApiModelProperty(value = "父级别元数据名称")
    private String parentName;

    /**
     * 父级别元数据Id
     */
    @ApiModelProperty(value = "父级别元数据Id")
    private Integer parentId;

    /**
     * 变更人id
     */
    @ApiModelProperty(value = "变更人")
    private String ownerId;

    /**
     * 变更时间
     */
    @ApiModelProperty(value = "变更时间")
    private LocalDateTime createTime;

    /**
     * 变更内容
     */
    @ApiModelProperty(value = "变更内容")
    private String changeContent;

    /**
     * 影响性分析 该字段变更影响到的元数据
     */
    @ApiModelProperty(value = "影响性分析 该字段变更影响到的元数据")
    private List<String> ImpactAnalysis;


}
