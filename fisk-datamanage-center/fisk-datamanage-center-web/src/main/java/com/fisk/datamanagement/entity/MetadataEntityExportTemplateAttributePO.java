package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

import java.io.Serializable;

/**
 * @author JinXingWang
 * @TableName tb_metadata_entity_export_template_attribute
 */
@TableName(value = "tb_metadata_entity_export_template_attribute")
@Data
public class MetadataEntityExportTemplateAttributePO extends BasePO implements Serializable {
    /**
     *
     */
    private Integer templateId;

    /**
     *
     */
    private Integer attributeId;

    /**
     *
     */
    private Integer attributePid;

    /**
     *
     */
    private String attributeName;

    /**
     *
     */
    private Boolean checked;

}