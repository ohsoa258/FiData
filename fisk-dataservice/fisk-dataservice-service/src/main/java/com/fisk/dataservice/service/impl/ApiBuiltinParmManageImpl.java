package com.fisk.dataservice.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataservice.entity.BuiltinParmPO;
import com.fisk.dataservice.mapper.ApiBuiltinParmMapper;
import com.fisk.dataservice.service.IApiBuiltinParmManageService;
import org.springframework.stereotype.Service;

/**
 * 内置参数接口实现类
 *
 * @author dick
 */
@Service
public class ApiBuiltinParmManageImpl extends ServiceImpl<ApiBuiltinParmMapper, BuiltinParmPO> implements IApiBuiltinParmManageService {

}
