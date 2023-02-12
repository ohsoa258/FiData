package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.dto.metadatamapatlas.MetaDataClassificationMapDTO;
import com.fisk.datamanagement.dto.metadatamapatlas.MetaDataGlossaryMapDTO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @ClassName:
 * @Author: 湖~Tloml
 * @Date: 2023
 * @Copyright: 2023 by 湖~Tloml
 * @Description:
 **/
@Mapper
public interface MetaDataClassificationMapMapper extends FKBaseMapper<MetaDataClassificationMapDTO> {
}
