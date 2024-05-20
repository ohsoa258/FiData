package com.fisk.datamanagement.mapper;

import com.fisk.datamanagement.entity.MetaSyncTimePO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;

/**
* @author 56263
* @description 针对表【tb_meta_sync_time】的数据库操作Mapper
* @createDate 2024-05-17 09:57:37
* @Entity com.fisk.datamanagement.entity.MetaSyncTimePO
*/
public interface MetaSyncTimePOMapper extends BaseMapper<MetaSyncTimePO> {


    @Delete("truncate TABLE tb_meta_sync_time")
    int truncateTable();

}




