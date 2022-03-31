package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.framework.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.ComponentNotificationPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 组件通知关联
 * @date 2022/3/23 12:42
 */
@Mapper
public interface ComponentNotificationMapper extends FKBaseMapper<ComponentNotificationPO> {

    /**
     * 修改关联通知有效性
     *
     * @param templateId 模板id
     * @param moduleId   组件id
     * @param noticeIds  通知id集合
     * @return 操作结果
     */
    int updateBy(@Param("templateId") int templateId, @Param("moduleId") int moduleId, @Param("noticeIds") List<Integer> noticeIds);

    /**
     * 修改关联通知有效性
     *
     * @param templateIds 模板id集合
     * @param moduleIds   组件id集合
     * @param noticeId    通知id
     * @return 操作结果
     */
    int updateByModuleIds( @Param("noticeId") int noticeId, @Param("templateIds") List<Integer> templateIds, @Param("moduleIds") List<Integer> moduleIds);
}
