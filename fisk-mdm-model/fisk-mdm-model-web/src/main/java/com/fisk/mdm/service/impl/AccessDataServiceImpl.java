package com.fisk.mdm.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.mdm.entity.AccessDataPO;
import com.fisk.mdm.mapper.AccessDataMapper;
import com.fisk.mdm.service.AccessDataService;
import org.springframework.stereotype.Service;

@Service("accessDataService")
public class AccessDataServiceImpl extends ServiceImpl<AccessDataMapper, AccessDataPO> implements AccessDataService {


}
