package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.dataservice.entity.AppWhiteListConfigPO;
import com.fisk.dataservice.mapper.AppWhiteListMapper;
import com.fisk.dataservice.service.IAppWhiteListManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author dick
 * @version 1.0
 * @description 应用白名单
 * @date 2023/6/7 9:43
 */
@Service
@Slf4j
public class AppWhiteListManageImpl extends ServiceImpl<AppWhiteListMapper, AppWhiteListConfigPO>
        implements IAppWhiteListManageService {
}
