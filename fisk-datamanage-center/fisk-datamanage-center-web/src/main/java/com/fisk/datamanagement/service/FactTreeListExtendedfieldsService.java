package com.fisk.datamanagement.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fisk.datamanagement.entity.FactTreePOs;

import java.util.List;

public interface FactTreeListExtendedfieldsService extends IService<FactTreePOs> {

    List<FactTreePOs> addFactTreeListExtendedfields(String pid);
}
