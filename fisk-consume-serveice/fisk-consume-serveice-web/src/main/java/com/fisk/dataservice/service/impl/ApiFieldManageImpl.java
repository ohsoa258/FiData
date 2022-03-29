package com.fisk.dataservice.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataservice.entity.FieldConfigPO;
import com.fisk.dataservice.mapper.ApiFieldMapper;
import com.fisk.dataservice.service.IApiFieldManageService;
import org.springframework.stereotype.Service;

/**
 * 字段接口实现类
 *
 * @author dick
 */
@Service
public class ApiFieldManageImpl extends ServiceImpl<ApiFieldMapper, FieldConfigPO> implements IApiFieldManageService {

}
