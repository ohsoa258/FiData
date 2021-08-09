package com.fisk.task.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.task.entity.TBETLlogPO;
import org.apache.ibatis.annotations.Param;

/**
 * @author: DennyHui
 * CreateTime: 2021/8/2 13:56
 * Description:
 */
public interface TBETLLogMapper extends BaseMapper<TBETLlogPO> {
    /**
     * 获取本次导入条数
     * @param dbname
     * @param code
     * @return
     */
    @DS("dorisdb")
    Integer getThisTimeStgRows(@Param("tablename") String dbname,@Param("code") String code);

    /**
     * stg to ods
     * @param ods_tablename
     * @param stg_tablename
     * @param code
     * @return
     */
    @DS("dorisdb")
    Integer stgToOds(@Param("ods_tablename") String ods_tablename,@Param("stg_tablename") String stg_tablename,@Param("fk_doris_increment_code") String code);
}