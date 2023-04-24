package com.fisk.datamanagement.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datamanagement.entity.MetaDataEntityOperationLogPO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-08 10:56
 * @description
 */
@Mapper
public interface MetaDataEntityOperationLogMapper extends FKBaseMapper<MetaDataEntityOperationLogPO> {
    @Select("SELECT tgl.* FROM tb_metadata_entity tme \n" +
            "JOIN tb_metadata_entity_operation_log tgl ON tme.id= tgl.metadata_entity_id\n" +
            "WHERE tgl.metadata_entity_id =#{entityId} AND tme.type_id=#{typeId}")
    List<MetaDataEntityOperationLogPO> selectOperationLog(@Param("entityId")Integer entityId,@Param("typeId")Integer typeId);

    @Delete("truncate TABLE tb_metadata_entity_operation_log ")
    int truncateTable();

}
