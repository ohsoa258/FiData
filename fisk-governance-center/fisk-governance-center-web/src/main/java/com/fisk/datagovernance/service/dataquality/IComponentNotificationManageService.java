package com.fisk.datagovernance.service.dataquality;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.common.response.ResultEnum;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckDTO;
import com.fisk.datagovernance.dto.dataquality.notice.ComponentNotificationDTO;
import com.fisk.datagovernance.entity.dataquality.ComponentNotificationPO;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 组件通知关联接口
 * @date 2022/3/23 12:22
 */
public interface IComponentNotificationManageService extends IService<ComponentNotificationPO> {
    /**
     * 添加数据
     *
     * @param templateId 模板id
     * @param moduleId   组件id
     * @param dto        关联通知集合
     * @param isDel      是否先置为无效
     * @return 执行结果
     */
    ResultEnum saveData(int templateId, long moduleId, List<ComponentNotificationDTO> dto, boolean isDel);

    /**
     * 添加数据
     *
     * @param noticeId   通知id
     * @param dto        关联通知集合
     * @param isDel      是否先置为无效
     * @return 执行结果
     */
    ResultEnum saveNoticeData(long noticeId, List<ComponentNotificationDTO> dto, boolean isDel);
}