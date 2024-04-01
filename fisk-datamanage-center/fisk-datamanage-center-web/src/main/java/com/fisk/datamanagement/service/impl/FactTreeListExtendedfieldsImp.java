package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamanagement.entity.FactTreePOs;
import com.fisk.datamanagement.mapper.FactTreeListMapper;
import com.fisk.datamanagement.service.FactTreeListExtendedfieldsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class FactTreeListExtendedfieldsImp extends ServiceImpl<FactTreeListMapper, FactTreePOs> implements FactTreeListExtendedfieldsService {
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
