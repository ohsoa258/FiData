package com.fisk.mdm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.mdm.entity.AccessTransformationPO;
import com.fisk.mdm.mapper.AccessTransformationMapper;
import com.fisk.mdm.service.AccessTransformationService;
import org.springframework.stereotype.Service;

@Service("accessTransformationService")
public class AccessTransformationServiceImpl extends ServiceImpl<AccessTransformationMapper, AccessTransformationPO> implements AccessTransformationService {


}
