package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.user.UserHelper;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterEditDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterQueryDTO;
import com.fisk.datagovernance.dto.dataquality.businessfilter.BusinessFilterSortDto;
import com.fisk.datagovernance.entity.dataquality.BusinessFilterPO;
import com.fisk.datagovernance.entity.dataquality.ComponentNotificationPO;
import com.fisk.datagovernance.map.dataquality.BusinessFilterMap;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilterMapper;
import com.fisk.datagovernance.mapper.dataquality.ComponentNotificationMapper;
import com.fisk.datagovernance.service.dataquality.IBusinessFilterManageService;
import com.fisk.datagovernance.vo.dataquality.businessfilter.BusinessFilterVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author dick
 * @version 1.0
 * @description 业务清洗
 * @date 2022/3/23 12:56
 */
@Service
public class BusinessFilterManageImpl extends ServiceImpl<BusinessFilterMapper, BusinessFilterPO> implements IBusinessFilterManageService {

    @Resource
    private BusinessFilterMapper businessFilterMapper;

    @Resource
    private ComponentNotificationMapper componentNotificationMapper;

    @Resource
    private ComponentNotificationMapImpl componentNotificationMapImpl;

    @Resource
    private BusinessFilterManageImpl businessFilterManageImpl;

    @Resource
    UserHelper userHelper;

    @Override
    public Page<BusinessFilterVO> getAll(BusinessFilterQueryDTO query) {
        Page<BusinessFilterVO> all = baseMapper.getAll(query.page, query.tableName, query.keyword);
        if (all != null && CollectionUtils.isNotEmpty(all.getRecords())) {
            List<Integer> collect = all.getRecords().stream().map(BusinessFilterVO::getId).distinct().collect(Collectors.toList());
            List<Integer> collect1 = all.getRecords().stream().map(BusinessFilterVO::getTemplateId).distinct().collect(Collectors.toList());
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
    public ResultEnum addData(BusinessFilterDTO dto) {
        //第一步：转换DTO对象为PO对象
        BusinessFilterPO businessFilterPO = BusinessFilterMap.INSTANCES.dtoToPo(dto);
        if (businessFilterPO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第二步：保存数据校验信息
        businessFilterPO.setCreateTime(LocalDateTime.now());
        businessFilterPO.setCreateUser(String.valueOf(userHelper.getLoginUserInfo().getId()));
        int i = baseMapper.insertOne(businessFilterPO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第三步：保存数据组件通知信息
        ResultEnum resultEnum = componentNotificationMapImpl.saveData(businessFilterPO.templateId, businessFilterPO.getId(), dto.componentNotificationDTOS, false);
        //第四步：如果设置了调度任务条件，则生成调度任务
        return resultEnum;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editData(BusinessFilterEditDTO dto) {
        BusinessFilterPO businessFilterPO = baseMapper.selectById(dto.id);
        if (businessFilterPO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        //第一步：转换DTO对象为PO对象
        businessFilterPO = BusinessFilterMap.INSTANCES.dtoToPo_Edit(dto);
        if (businessFilterPO == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第二步：保存数据校验信息
        int i = baseMapper.updateById(businessFilterPO);
        if (i <= 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        //第三步：保存数据组件通知信息，先将原来的组件通知关联关系置为无效
        ResultEnum resultEnum = componentNotificationMapImpl.saveData(businessFilterPO.templateId, businessFilterPO.getId(), dto.componentNotificationDTOS, true);
        //第四步：根据组件状态&Corn表达式，调整调度任务
        return resultEnum;
    }

    @Override
    public ResultEnum deleteData(int id) {
        BusinessFilterPO businessFilterPO = baseMapper.selectById(id);
        if (businessFilterPO == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        return baseMapper.deleteByIdWithFill(businessFilterPO) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum editModuleExecSort(List<BusinessFilterSortDto> dto) {
        if (CollectionUtils.isEmpty(dto)) {
            return ResultEnum.DATA_QUALITY_REQUESTSORT_ERROR;
        }
        List<Integer> collect = dto.stream().map(BusinessFilterSortDto::getId).distinct().collect(Collectors.toList());
        QueryWrapper<BusinessFilterPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(BusinessFilterPO::getDelFlag, 1).in(BusinessFilterPO::getId, collect);
        List<BusinessFilterPO> businessFilterPOS = businessFilterMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(businessFilterPOS)) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        businessFilterPOS.forEach(e -> {
            Optional<BusinessFilterSortDto> first = dto.stream().filter(item -> item.getId() == e.getId()).findFirst();
            if (first.isPresent()) {
                BusinessFilterSortDto businessFilterSortDto = first.get();
                if (businessFilterSortDto != null) {
                    e.setModuleExecSort(businessFilterSortDto.getModuleExecSort());
                }
            }
        });
        boolean b = businessFilterManageImpl.updateBatchById(businessFilterPOS);
        return b ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }
}
