package com.fisk.datafactory.mapper;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datafactory.entity.NifiCustomWorkflowDetailPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author Lock
 */
@Mapper
public interface NifiCustomWorkflowDetailMapper extends FKBaseMapper<NifiCustomWorkflowDetailPO> {

    @Update("update tb_nifi_custom_workflow_detail set del_flag=0 where del_flag=1 and pid!=0 and workflow_id = #{nifiCustomWorkflowDetail.workflowId} and component_type = #{nifiCustomWorkflowDetail.componentType}")
    void deleteByType(@Param("nifiCustomWorkflowDetail")NifiCustomWorkflowDetailPO nifiCustomWorkflowDetail);
}
