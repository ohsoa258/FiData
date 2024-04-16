package com.fisk.system.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
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

    @Select("SELECT t2.role_id FROM `tb_service_registry` t1 LEFT JOIN tb_role_service_assignment t2 on t1.id = t2.service_id where t1.del_flag = 1 and t2.del_flag = 1 and t1.serve_url = \"dataAsset_assetJnj\" and t2.switch_authorization = 1")
    List<Integer> getBusinessAssignmentRoleId();
}
