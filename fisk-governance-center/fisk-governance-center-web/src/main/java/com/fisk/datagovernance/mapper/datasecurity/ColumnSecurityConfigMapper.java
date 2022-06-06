package com.fisk.datagovernance.mapper.datasecurity;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.dto.datasecurity.columnsecurityconfig.ColumnSecurityConfigUserAssignmentDTO;
import com.fisk.datagovernance.entity.datasecurity.ColumnSecurityConfigPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author lock
 * @email feihongz@fisksoft.com.cn
 * @date 2022-03-28 15:47:33
 */
@Mapper
public interface ColumnSecurityConfigMapper extends FKBaseMapper<ColumnSecurityConfigPO> {

    /**
     * 添加列级配置
     * @param dto
     * @return
     */
    int insertColumnSecurityConfig(@Param("dto") ColumnSecurityConfigUserAssignmentDTO dto);


}
