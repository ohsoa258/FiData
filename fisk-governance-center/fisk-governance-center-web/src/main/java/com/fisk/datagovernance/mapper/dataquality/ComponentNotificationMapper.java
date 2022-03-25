package com.fisk.datagovernance.mapper.dataquality;

import com.fisk.common.mybatis.FKBaseMapper;
import com.fisk.datagovernance.entity.dataquality.ComponentNotificationPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

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
    @Update("UPDATE tb_component_notification SET del_flag=0 WHERE template_id=#{templateId} AND module_id=#{moduleId} AND notice_id IN(#{noticeIds});")
    int updateBy(@Param("templateId") int templateId, @Param("moduleId") int moduleId, @Param("noticeIds") String noticeIds);

    /**
     * 修改关联通知有效性
     *
     * @param templateIds 模板id集合
     * @param moduleIds   组件id集合
     * @param noticeId    通知id
     * @return 操作结果
     */
    @Update("UPDATE tb_component_notification SET del_flag=0 WHERE template_id IN (#{templateIds}) AND notice_id=#{noticeId} AND module_id IN(#{moduleIds});")
    int updateByModuleIds(@Param("templateIds") String templateIds, @Param("noticeId") int noticeId, @Param("moduleIds") String moduleIds);
}
