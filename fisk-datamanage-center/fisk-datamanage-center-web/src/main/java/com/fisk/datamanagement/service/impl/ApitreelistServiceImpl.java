package com.fisk.datamanagement.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datamanagement.entity.ApitreePO;
import com.fisk.datamanagement.mapper.ApitreelistMapper;
import com.fisk.datamanagement.service.ApitreelistService;
import org.springframework.stereotype.Service;

@Service("apitreelistService")
public class ApitreelistServiceImpl extends ServiceImpl<ApitreelistMapper, ApitreePO> implements ApitreelistService {


}
