package com.fisk.dataservice.map;

import com.fisk.dataservice.entity.FileServicePO;
import com.fisk.dataservice.vo.fileservice.FileServiceVO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FileServiceMap {

    FileServiceMap INSTANCES = Mappers.getMapper(FileServiceMap.class);

    /**
     * poList==>VoList
     *
     * @param poList
     * @return
     */
    List<FileServiceVO> poListToVoList(List<FileServicePO> poList);

}
