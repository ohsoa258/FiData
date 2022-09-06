package com.fisk.dataaccess.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataaccess.dto.datatargetapp.DataTargetAppDTO;
import com.fisk.dataaccess.entity.DataTargetAppPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author JianWenYang
 */
@Mapper
public interface DataTargetAppMapper extends FKBaseMapper<DataTargetAppPO> {

    /**
     * 分页查询
     *
     * @param page
     * @param query
     * @return
     */
    Page<DataTargetAppDTO> queryList(Page<DataTargetAppDTO> page, @Param("query") String query);

}
