package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.metadataentityexporttemplate.MetadataExportTemplateAttributeDto;
import com.fisk.datamanagement.dto.metadataentityexporttemplate.MetadataExportTemplateDto;
import com.fisk.datamanagement.entity.MetadataEntityExportTemplateAttributePO;


import java.util.List;

/**
* @author JinXingWang
* @description 针对表【tb_metadata_entity_export_template_attribute】的数据库操作Service
* @createDate 2023-08-16 16:18:32
*/
public interface IMetadataEntityExportTemplateAttributeService extends IService<MetadataEntityExportTemplateAttributePO> {
    /**
     * 通过模板ID获取模板下的属性树形结构
     * @param templateId
     * @return
     */
    List<MetadataExportTemplateAttributeDto> getTemplateAttributeTreeByTemplateId(Integer templateId);

    /**
     * 通过模板ID获取模板下的显示的属性
     * @param templateId
     * @return
     */
    List<MetadataExportTemplateAttributeDto> getTemplateValidityAttributeByTemplateId(Integer templateId);

    /**
     * 添加导出模板下的属性
     * @param templateId
     * @param dtoList
     * @return
     */

    ResultEnum addBatch(Integer templateId, List<MetadataExportTemplateAttributeDto> dtoList);

    /**
     * 修改导出模板下的属性
     * @param templateId
     * @param dtoList
     * @return
     */
    ResultEnum  editBatch(Integer templateId , List<MetadataExportTemplateAttributeDto> dtoList);


}
