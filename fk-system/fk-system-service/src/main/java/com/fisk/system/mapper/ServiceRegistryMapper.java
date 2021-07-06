package com.fisk.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.system.entity.ServiceRegistryPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author JianWenYang
 * * @data: 2021/7/06 13:55
 */
@Mapper
public interface ServiceRegistryMapper extends BaseMapper<ServiceRegistryPO> {
    /**
     * 查询appName
     * @return 返回值
     */
    @Select("select serve_cn_name from tb_service_registry")
    List<String> getServiceName();
}
