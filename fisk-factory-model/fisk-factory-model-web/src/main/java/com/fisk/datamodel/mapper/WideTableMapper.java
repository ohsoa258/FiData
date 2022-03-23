package com.fisk.datamodel.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datamodel.entity.WideTableConfigPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author JianWenYang
 */
@Mapper
public interface WideTableMapper extends FKBaseMapper<WideTableConfigPO> {

}
