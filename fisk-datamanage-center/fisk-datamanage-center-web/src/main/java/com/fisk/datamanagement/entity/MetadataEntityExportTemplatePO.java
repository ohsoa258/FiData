package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

import java.io.Serializable;

/**
 * @author JinXingWang
 * @TableName tb_metadata_entity_export_template
 */
@TableName(value = "tb_metadata_entity_export_template")
@Data
public class MetadataEntityExportTemplatePO extends BasePO implements Serializable {


    private String name;

}