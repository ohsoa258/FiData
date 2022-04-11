package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.datagovernance.dto.dataquality.lifecycle.LifecycleDTO;
import com.fisk.datagovernance.dto.dataquality.lifecycle.LifecycleEditDTO;
import com.fisk.datagovernance.dto.dataquality.lifecycle.LifecycleQueryDTO;
import com.fisk.datagovernance.entity.dataquality.ComponentNotificationPO;
import com.fisk.datagovernance.entity.dataquality.LifecyclePO;
import com.fisk.datagovernance.entity.dataquality.TemplatePO;
import com.fisk.datagovernance.map.dataquality.LifecycleMap;
import com.fisk.datagovernance.mapper.dataquality.ComponentNotificationMapper;
import com.fisk.datagovernance.mapper.dataquality.LifecycleMapper;
import com.fisk.datagovernance.mapper.dataquality.TemplateMapper;
import com.fisk.datagovernance.service.dataquality.ILifecycleManageService;
import com.fisk.datagovernance.vo.dataquality.lifecycle.LifecycleVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 生命周期实现类
 * @date 2022/3/23 12:56
 */
@Service
public class LifecycleManageImpl extends ServiceImpl<LifecycleMapper, LifecyclePO> implements ILifecycleManageService {

    @Resource
    private ComponentNotificationMapper componentNotificationMapper;

    @Resource
    private ComponentNotificationMapImpl componentNotificationMapImpl;

    @Resource
    private TemplateMapper templateMapper;

    @Resource
    UserHelper userHelper;

    @Override
    public Page<LifecycleVO> getAll(LifecycleQueryDTO query) {
        Page<LifecycleVO> all = baseMapper.getAll(query.page, query.tableName, query.keyword);
        if (all != null && CollectionUtils.isNotEmpty(all.getRecords())) {
            List<Integer> collect = all.getRecords().stream().map(LifecycleVO::getId).distinct().collect(Collectors.toList());
            List<Integer> collect1 = all.getRecords().stream().map(LifecycleVO::getTemplateId).distinct().collect(Collectors.toList());
            QueryWrapper<ComponentNotificationPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(ComponentNotificationPO::getDelFlag, 1)
                    .in(ComponentNotificationPO::getTemplateId, collect1)
                    .in(ComponentNotificationPO::getModuleId, collect);
            List<ComponentNotificationPO> componentNotificationPOS = componentNotificationMapper.selectList(queryWrapper);
            if (CollectionUtils.isNotEmpty(componentNotificationPOS)) {
                all.getRecords().forEach(e -> {
                    List<Integer> collect2 = componentNotificationPOS.stream()
                            .filter(item -> item.getTemplateId() == e.getTemplateId() && item.getModuleId() == e.getId())
                            .map(ComponentNotificationPO::getNoticeId).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(collect2)) {
                        e.setNoticeIds(collect2);
                    }
                });
            }
        }
        return all;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(LifecycleDTO dto) {
        //验证模板是否存在
        TemplatePO templatePO = templateMapper.selectById(dto.templateId);
        if (templatePO == null) {
            return ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS;
        }
        //第一步：转换DTO对象为PO对象
        LifecyclePO lifecyclePO = LifecycleMap.INSTANCES.dtoToPo(dto);
        if (lifecyclePO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第二步：保存数据校验信息
        lifecyclePO.setCreateTime(LocalDateTime.now());
        lifecyclePO.setCreateUser(String.valueOf(userHelper.getLoginUserInfo().getId()));
        int i = baseMapper.insertOne(lifecyclePO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第三步：保存数据组件通知信息
        ResultEnum resultEnum = componentNotificationMapImpl.saveData(lifecyclePO.templateId, lifecyclePO.getId(), dto.componentNotificationDTOS, false);
        //第四步：如果设置了调度任务条件，则生成调度任务
        return resultEnum;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editData(LifecycleEditDTO dto) {
        //验证模板是否存在
        TemplatePO templatePO = templateMapper.selectById(dto.templateId);
        if (templatePO == null) {
            return ResultEnum.DATA_QUALITY_TEMPLATE_EXISTS;
        }
        LifecyclePO lifecyclePO = baseMapper.selectById(dto.id);
        if (lifecyclePO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //第一步：转换DTO对象为PO对象
        lifecyclePO = LifecycleMap.INSTANCES.dtoToPo_Edit(dto);
        if (lifecyclePO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第二步：保存数据校验信息
        int i = baseMapper.updateById(lifecyclePO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第三步：保存数据组件通知信息，先将原来的组件通知关联关系置为无效
        ResultEnum resultEnum = componentNotificationMapImpl.saveData(lifecyclePO.templateId, lifecyclePO.getId(), dto.componentNotificationDTOS, true);
        //第四步：根据组件状态&Corn表达式，调整调度任务
        return resultEnum;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum deleteData(int id) {
        LifecyclePO lifecyclePO = baseMapper.selectById(id);
        if (lifecyclePO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        componentNotificationMapImpl.updateDelFlag(0, lifecyclePO.getTemplateId(), lifecyclePO.getId());
        return baseMapper.deleteByIdWithFill(lifecyclePO) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

}