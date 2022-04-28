package com.fisk.mdm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.mdm.dto.attribute.AttributeQueryDTO;
import com.fisk.mdm.entity.AttributePO;
import com.fisk.mdm.vo.attribute.AttributeVO;
import com.fisk.mdm.vo.entity.EntityMsgVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author WangYan
 * @date 2022/4/5 12:30
 */
@Mapper
public interface AttributeMapper extends BaseMapper<AttributePO> {

    /**
     * 分页查询
     * @param page
     * @param query
     * @return
     */
    Page<AttributeVO> getAll(Page<AttributeVO> page, @Param("query") AttributeQueryDTO query);

    /**
     * 获取实体ER图信息
     *
     * @return {@link List}<{@link EntityMsgVO}>
     */
    List<EntityMsgVO> getER();

    /**
     * 删除属性(修改状态为删除待提交)
     *
     * @param id 属性id
     * @return {@link ResultEnum}
     */
    int deleteAttribute(Integer id);

}
