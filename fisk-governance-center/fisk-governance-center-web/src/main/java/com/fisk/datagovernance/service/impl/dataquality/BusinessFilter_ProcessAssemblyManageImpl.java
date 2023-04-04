package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datagovernance.entity.dataquality.BusinessFilter_ProcessAssemblyPO;
import com.fisk.datagovernance.map.dataquality.BusinessFilter_ProcessAssemblyMap;
import com.fisk.datagovernance.mapper.dataquality.BusinessFilter_ProcessAssemblyMapper;
import com.fisk.datagovernance.service.dataquality.IBusinessFilter_ProcessAssemblyManageService;
import com.fisk.datagovernance.vo.dataquality.businessfilter.process.BusinessFilter_ProcessAssemblyVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusinessFilter_ProcessAssemblyManageImpl
        extends ServiceImpl<BusinessFilter_ProcessAssemblyMapper, BusinessFilter_ProcessAssemblyPO>
        implements IBusinessFilter_ProcessAssemblyManageService {

    @Override
    public List<BusinessFilter_ProcessAssemblyPO> getPOList() {
        QueryWrapper<BusinessFilter_ProcessAssemblyPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(BusinessFilter_ProcessAssemblyPO::getDelFlag, 1)
                .orderByAsc(BusinessFilter_ProcessAssemblyPO::getAssemblySort);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<BusinessFilter_ProcessAssemblyVO> getVOList() {
        List<BusinessFilter_ProcessAssemblyVO> voList = null;
        List<BusinessFilter_ProcessAssemblyPO> poList = getPOList();
        if (CollectionUtils.isNotEmpty(poList)) {
            voList = BusinessFilter_ProcessAssemblyMap.INSTANCES.poListToVoList(poList);
        }
        return voList;
    }
}
