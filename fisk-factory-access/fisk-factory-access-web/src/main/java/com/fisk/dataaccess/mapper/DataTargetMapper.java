package com.fisk.dataaccess.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.dataaccess.dto.output.datatarget.DataTargetAddDTO;
import com.fisk.dataaccess.dto.output.datatarget.DataTargetPageResultDTO;
import com.fisk.dataaccess.entity.DataTargetPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author JianWenYang
 */
@Mapper
public interface DataTargetMapper extends FKBaseMapper<DataTargetPO> {

    /**
     * 分页查询数据目标
     *
     * @param page
     * @param query
     * @param dataTargetAppId
     * @return
     */
    Page<DataTargetPageResultDTO> queryList(Page<DataTargetPageResultDTO> page, @Param("query") String query, @Param("dataTargetAppId") Long dataTargetAppId);

    /**
     * 新增数据目标
     *
     * @param dto
     * @return
     */
    Integer insertDataTarget(@Param("dataTargetDto") DataTargetAddDTO dto);

}
