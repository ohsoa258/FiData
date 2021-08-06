package com.fisk.dataaccess.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.dataaccess.entity.NifiConfigPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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
    @Select("SELECT `component_key` FROM tb_nifi_config;")
    String getNifiKey();

    /**
     * 获取value
     *
     * @return valeu
     */
    @Select("SELECT `component_id` FROM tb_nifi_config;")
    String getNifiValue();

    /**
     * 添加
     *
     * @param key 唯一key
     * @param value value
     * @return 执行结果
     */
    @Insert("insert into tb_nifi_config(`key`,`value`) values(#{key},#{value})")
    int addNifiConfig(@Param("key") String key, @Param("value") String value);
}
