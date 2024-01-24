package com.fisk.datamanagement.service.impl;

import com.fisk.datamanagement.entity.BusinessExtendedfieldsPO;
import com.fisk.datamanagement.entity.FactTreePOs;
import com.fisk.datamanagement.mapper.BusinessExtendedfieldsMapper;
import com.fisk.datamanagement.mapper.BusinessTargetinfoMapper;
import com.fisk.datamanagement.mapper.FactTreeListMapper;
import com.fisk.datamanagement.service.FactTreeListExtendedfieldsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class FactTreeListExtendedfieldsImp implements FactTreeListExtendedfieldsService {
    @Resource
    FactTreeListMapper factTreeListMapper;

    @Override
    public List<FactTreePOs> addFactTreeListExtendedfields(String pid) {
        // 查询数据
        List<FactTreePOs> po = factTreeListMapper.selectParentpIds(pid);

        System.out.println(po);
        return po;

    }
}
