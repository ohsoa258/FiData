package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.entity.LineageMapRelationPO;
import org.apache.ibatis.annotations.Delete;

/**
 * @author JianWenYang
 */
public interface LineageMapRelationMapper extends FKBaseMapper<LineageMapRelationPO> {

    @Delete("truncate TABLE  tb_lineage_map_relation")
    int truncateTable();
}
