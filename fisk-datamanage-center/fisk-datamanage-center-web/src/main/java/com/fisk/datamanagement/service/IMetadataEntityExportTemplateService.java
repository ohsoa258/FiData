package com.fisk.datamanagement.service;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.metadataentityexporttemplate.EditMetadataExportTemplateDto;
import com.fisk.datamanagement.dto.metadataentityexporttemplate.MetadataExportTemplateDetailDto;
import com.fisk.datamanagement.dto.metadataentityexporttemplate.MetadataExportTemplateDto;
import com.fisk.datamanagement.entity.MetadataEntityExportTemplatePO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author JinXingWang
* @description 针对表【tb_metadata_entity_export_template】的数据库操作Service
* @createDate 2023-08-16 16:30:25
*/
public interface IMetadataEntityExportTemplateService extends IService<MetadataEntityExportTemplatePO> {
     /**
      * 获取导出模板数据
      * @param id
      * @return
      */
     MetadataExportTemplateDetailDto get(Integer id);

     /**
      * 添加模板
      * @param dto
      * @return
      */
     ResultEnum add(MetadataExportTemplateDetailDto dto);

     /**
      * 修改模板
      * @return
      */
     ResultEnum edit(EditMetadataExportTemplateDto dto);

     /**
      * 获取所有的模板
      * @return
      */
     List<MetadataExportTemplateDto> getAllTemplate();
}
