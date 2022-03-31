package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataservice.entity.ParmConfigPO;
import com.fisk.dataservice.mapper.ApiParmMapper;
import com.fisk.dataservice.service.IApiParmManageService;
import org.springframework.stereotype.Service;

/**
 * 参数接口实现类
 *
 * @author dick
 */
@Service
public class ApiParmManageImpl extends ServiceImpl<ApiParmMapper, ParmConfigPO> implements IApiParmManageService {

}
