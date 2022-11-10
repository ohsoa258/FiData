package com.fisk.datamodel.mapper.widetable;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamodel.dto.widetableconfig.WideTableConfigDTO;
import com.fisk.datamodel.entity.widetable.WideTableConfigPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author JianWenYang
 */
@Mapper
public interface WideTableMapper extends FKBaseMapper<WideTableConfigPO> {

    /**
     * 新增宽表,返回主键id
     *
     * @param dto
     * @return
     */
    int insertWideTable(@Param("dto") WideTableConfigDTO dto);
}
