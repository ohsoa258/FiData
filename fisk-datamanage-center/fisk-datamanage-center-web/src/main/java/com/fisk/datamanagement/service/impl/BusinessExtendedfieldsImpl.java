package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamanagement.entity.BusinessExtendedfieldsPO;
import com.fisk.datamanagement.mapper.BusinessExtendedfieldsMapper;
import com.fisk.datamanagement.service.BusinessExtendedfieldsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xgf
 * @date 2023年11月20日 10:44
 */
@Service
public class BusinessExtendedfieldsImpl extends ServiceImpl<BusinessExtendedfieldsMapper, BusinessExtendedfieldsPO> implements BusinessExtendedfieldsService {

    @Resource
    BusinessExtendedfieldsMapper businessExtendedfieldsMapper;
    /**
     * 展示维度数据
     *
     * @param
     */
    @Override
    public List<BusinessExtendedfieldsPO> addBusinessExtendedfields(String categoryId) {
        // 查询数据
        List<BusinessExtendedfieldsPO> po = businessExtendedfieldsMapper.selectParentpId(categoryId);
        System.out.println(po);
        return po;
    }

}
