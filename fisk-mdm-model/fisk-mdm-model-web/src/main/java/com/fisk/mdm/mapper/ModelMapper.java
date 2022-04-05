package com.fisk.mdm.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.mdm.entity.ModelPO;
import com.fisk.mdm.dto.model.ModelQueryDTO;
import com.fisk.mdm.vo.model.ModelVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ModelMapper extends FKBaseMapper<ModelPO> {

    /**
     * 分页查询
     * @param page
     * @param query
     * @return
     */
    Page<ModelVO> getAll(Page<ModelVO> page, @Param("query") ModelQueryDTO query);
}
