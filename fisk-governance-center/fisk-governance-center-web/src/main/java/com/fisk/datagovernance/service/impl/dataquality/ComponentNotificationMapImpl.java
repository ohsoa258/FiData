package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datagovernance.dto.dataquality.notice.ComponentNotificationDTO;
import com.fisk.datagovernance.entity.dataquality.ComponentNotificationPO;
import com.fisk.datagovernance.map.dataquality.ComponentNotificationMap;
import com.fisk.datagovernance.mapper.dataquality.ComponentNotificationMapper;
import com.fisk.datagovernance.service.dataquality.IComponentNotificationManageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 组件通知关联
 * @date 2022/3/25 15:42
 */
@Service
public class ComponentNotificationMapImpl extends ServiceImpl<ComponentNotificationMapper, ComponentNotificationPO> implements IComponentNotificationManageService {

    @Resource
    private ComponentNotificationMapImpl componentNotificationMapImpl;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum saveData(int templateId, long moduleId, List<ComponentNotificationDTO> dto, boolean isDel) {
        int moduleIdValue = Math.toIntExact(moduleId);
        if (CollectionUtils.isNotEmpty(dto)) {
            dto.forEach(e -> {
                e.templateId = templateId;
                e.moduleId = moduleIdValue;
            });
            List<ComponentNotificationPO> componentNotificationPOS = ComponentNotificationMap.INSTANCES.listDtoToPo(dto);
            if (CollectionUtils.isEmpty(componentNotificationPOS)) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
            if (isDel) {
                List<Integer> noticeIds = componentNotificationPOS.stream().map(ComponentNotificationPO::getNoticeId).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(noticeIds)) {
                    baseMapper.updateBy(templateId, moduleIdValue, noticeIds);
                }
            }
            boolean b = componentNotificationMapImpl.saveBatch(componentNotificationPOS);
            if (!b) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
        }
        return ResultEnum.SUCCESS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum saveNoticeData(long noticeId, List<ComponentNotificationDTO> dto, boolean isDel) {
        int noticeIdValue = Math.toIntExact(noticeId);
        if (CollectionUtils.isNotEmpty(dto)) {
            dto.forEach(e -> {
                e.noticeId = noticeIdValue;
            });
            List<ComponentNotificationPO> componentNotificationPOS = ComponentNotificationMap.INSTANCES.listDtoToPo(dto);
            if (CollectionUtils.isEmpty(componentNotificationPOS)) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
            if (isDel) {
                List<Integer> moduleIds = componentNotificationPOS.stream().map(ComponentNotificationPO::getModuleId).distinct().collect(Collectors.toList());
                List<Integer> templateIds = componentNotificationPOS.stream().map(ComponentNotificationPO::getTemplateId).distinct().collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(moduleIds) && CollectionUtils.isNotEmpty(templateIds)) {
                    baseMapper.updateByModuleIds(noticeIdValue, templateIds, moduleIds);
                }
            }
            boolean b = componentNotificationMapImpl.saveBatch(componentNotificationPOS);
            if (!b) {
                return ResultEnum.SAVE_DATA_ERROR;
            }
        }
        return ResultEnum.SUCCESS;
    }
}
