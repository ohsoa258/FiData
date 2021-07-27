package com.fisk.dataaccess.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.dataaccess.entity.NifiConfigPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author Lock
 */
@Mapper
public interface NifiConfigMapper extends BaseMapper<NifiConfigPO> {

    /**
     * 获取key
     *
     * @return key
     */
    @Select("SELECT `key` FROM tb_nifi_config;")
    String getNifiKey();

    /**
     * 获取value
     *
     * @return valeu
     */
    @Select("SELECT `value` FROM tb_nifi_config;")
    String getNifiValue();
}
