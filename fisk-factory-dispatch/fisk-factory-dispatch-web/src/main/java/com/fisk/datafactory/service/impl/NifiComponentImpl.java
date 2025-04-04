package com.fisk.datafactory.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.datafactory.entity.NifiComponentsPO;
import com.fisk.datafactory.mapper.NifiComponentsMapper;
import com.fisk.datafactory.service.INifiComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author wangyan and Lock
 */
@Service
@Slf4j
public class NifiComponentImpl extends ServiceImpl<NifiComponentsMapper, NifiComponentsPO> implements INifiComponent {
}
