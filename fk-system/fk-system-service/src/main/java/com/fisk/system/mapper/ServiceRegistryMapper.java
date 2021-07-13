package com.fisk.system.mapper;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.system.entity.ServiceRegistryPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author JianWenYang
 */
@Mapper
public interface ServiceRegistryMapper extends FKBaseMapper<ServiceRegistryPO> {
    /**
     * 查询appName
     * @return 返回值
     */
    @Select("select serve_cn_name from tb_service_registry")
    List<String> getServiceName();
}
