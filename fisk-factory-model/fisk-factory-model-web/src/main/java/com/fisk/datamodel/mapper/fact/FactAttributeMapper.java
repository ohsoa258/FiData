package com.fisk.datamodel.mapper.fact;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamodel.dto.factattribute.FactAttributeListDTO;
import com.fisk.datamodel.entity.fact.FactAttributePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper
public interface FactAttributeMapper extends FKBaseMapper<FactAttributePO> {

    /**
     * 获取事实字段表数据
     * @param factId
     * @return
     */
    List<FactAttributeListDTO> getFactAttributeList(@Param("factId") int factId);

}
