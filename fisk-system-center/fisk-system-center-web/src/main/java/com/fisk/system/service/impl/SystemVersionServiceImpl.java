package com.fisk.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.system.dto.SystemVersionDTO;
import com.fisk.system.entity.SystemVersionPO;
import com.fisk.system.map.SystemVersionMap;
import com.fisk.system.mapper.SystemVersionMapper;
import com.fisk.system.service.SystemVersionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 56263
 * @description 针对表【tb_system_version】的数据库操作Service实现
 * @createDate 2023-04-07 14:05:19
 */
@Service
@Slf4j
public class SystemVersionServiceImpl extends ServiceImpl<SystemVersionMapper, SystemVersionPO>
        implements SystemVersionService {

    @Override
    public SystemVersionDTO get() {
        LambdaQueryWrapper<SystemVersionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SystemVersionPO::getId).last("limit 1");
        SystemVersionPO systemVersionPO = getOne(wrapper);
        //时间格式转换
        Date publishTime = systemVersionPO.getPublishTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        //转换为字符串 yyyy-MM
        String format = simpleDateFormat.format(publishTime);
        //po to dto
        SystemVersionDTO systemVersionDTO = SystemVersionMap.INSTANCES.poToDto(systemVersionPO);
        //设置时间字符串
        systemVersionDTO.setPublishTime(format);
        return systemVersionDTO;
    }

}




