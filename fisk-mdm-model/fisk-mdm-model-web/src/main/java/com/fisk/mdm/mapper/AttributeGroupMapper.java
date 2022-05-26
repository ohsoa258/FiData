package com.fisk.mdm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fisk.mdm.dto.attributeGroup.AttributeInfoDTO;
import com.fisk.mdm.entity.AttributeGroupPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author WangYan
 * @Date 2022/5/23 11:28
 * @Version 1.0
 */
@Mapper
public interface AttributeGroupMapper extends BaseMapper<AttributeGroupPO> {


    /**
     * 获取出属性组存在的属性
     * @param groupId
     * @param entityId
     * @return
     */
    @Select("SELECT t1.id,t1.NAME AS `name`,t1.display_name,t1.DESC AS `desc`,t1.data_type,t1.data_type_length,t1.data_type_decimal_length,t2.id AS exists_group FROM "
    + " `tb_attribute` t1 LEFT JOIN tb_attribute_group_details t2 ON t1.id = t2.attribute_id AND t2.del_flag = 1 AND t2.group_id = #{groupId} AND t2.entity_id = #{entityId} "
    + " WHERE t1.del_flag = 1 AND t1.entity_id = #{entityId} AND t1.`status` = 2 ")
    List<AttributeInfoDTO> getAttributeExists(@Param("groupId") Integer groupId,@Param("entityId") Integer entityId);
}
