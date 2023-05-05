package com.fisk.dataaccess.map.codepreview;

import com.fisk.common.service.accessAndTask.factorycodepreviewdto.PreviewTableBusinessDTO;
import com.fisk.common.service.accessAndTask.factorycodepreviewdto.PublishFieldDTO;
import com.fisk.dataaccess.dto.factorycodepreviewdto.AccessPublishFieldDTO;
import com.fisk.dataaccess.dto.table.TableBusinessDTO;
import com.fisk.task.dto.modelpublish.ModelPublishFieldDTO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccessCodePreviewMapper {

    AccessCodePreviewMapper INSTANCES = Mappers.getMapper(AccessCodePreviewMapper.class);

    /**
     * ModelPublishFieldDTO ==> PublishFieldDTO
     *
     * @param fieldList
     * @return
     */
    List<PublishFieldDTO> listToList(List<ModelPublishFieldDTO> fieldList);

    /**
     * TableBusinessDTO ==> PreviewTableBusinessDTO
     *
     * @param tableBusiness
     * @return
     */
    PreviewTableBusinessDTO dtoToDto(TableBusinessDTO tableBusiness);

    /**
     * List<AccessPublishFieldDTO>   ==>   List<PublishFieldDTO>
     *
     * @param fieldList
     * @return
     */
    List<PublishFieldDTO> tableFieldsToPublishFields(List<AccessPublishFieldDTO> fieldList);

}
