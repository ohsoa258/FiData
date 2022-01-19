package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataservice.entity.FilterConditionConfigPO;
import com.fisk.dataservice.mapper.ApiFilterConditionMapper;
import com.fisk.dataservice.service.IApiFilterConditionManageService;
import org.springframework.stereotype.Service;

/**
 * 过滤条件接口实现类
 *
 * @author dick
 */
@Service
public class ApiFilterConditionManageImpl extends ServiceImpl<ApiFilterConditionMapper, FilterConditionConfigPO> implements IApiFilterConditionManageService {

}
