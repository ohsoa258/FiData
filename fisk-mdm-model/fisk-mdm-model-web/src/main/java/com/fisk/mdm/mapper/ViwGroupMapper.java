package com.fisk.mdm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.mdm.dto.entity.EntityDTO;
import com.fisk.mdm.entity.EntityPO;
import com.fisk.mdm.entity.ViwGroupPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author WangYan
 * @date 2022/4/2 17:48
 */
@Mapper
public interface ViwGroupMapper extends BaseMapper<ViwGroupPO> {

    /**
     * 查询已经发布得实体(根据模型id)
     * @param modelId
     * @return
     */
    @Select("SELECT id,model_id,`name`,display_name,`desc`,`status`,table_name,enable_member_log,approval_rule_id,build_code_rule_id,hierarchy_id FROM viw_release_entity WHERE model_id = #{modelId} AND del_flag = '1'")
    List<EntityDTO> getReleaseData(@Param("modelId") int modelId);
}
