package com.fisk.datamanagement.service;

import com.fisk.datamanagement.entity.BusinessExtendedfieldsPO;
import com.fisk.datamanagement.entity.FactTreePOs;
import com.fisk.datamanagement.mapper.FactTreeListMapper;

import javax.annotation.Resource;
import java.util.List;

public interface FactTreeListExtendedfieldsService {

    List<FactTreePOs> addFactTreeListExtendedfields(String pid);
}
