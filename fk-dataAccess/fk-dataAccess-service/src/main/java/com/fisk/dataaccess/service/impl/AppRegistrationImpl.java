package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.AppRegistrationDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.entity.AppRegistrationPO;
import com.fisk.dataaccess.mapper.AppDataSourceMapper;
import com.fisk.dataaccess.mapper.AppRegistrationMapper;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.dataaccess.vo.AppDataSourceVO;
import com.fisk.dataaccess.vo.AppRegistrationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author: Lock
 * @data: 2021/5/26 14:13
 */
@Service
public class AppRegistrationImpl extends ServiceImpl<AppRegistrationMapper, AppRegistrationPO> implements IAppRegistration {

    @Resource
    private AppRegistrationMapper mapper;

    @Autowired
    private AppDataSourceImpl appDataSourceImpl;

    /**
     * 添加应用
     * @param appRegistrationDTO
     * @return
     */
    @Override
    @Transactional
    public ResultEnum addData(AppRegistrationDTO appRegistrationDTO) {

        // dto->po
        AppRegistrationPO appRegistrationPO = appRegistrationDTO.toEntity(AppRegistrationPO.class);

        // 保存基本信息
        appRegistrationPO.setId(UUID.randomUUID().toString());

        // 保存应用注册表数据
        this.save(appRegistrationPO);

        AppDataSourcePO appDatasourcePO = appRegistrationDTO.getAppDatasourceDTO().toEntity(AppDataSourcePO.class);

        appDatasourcePO.setId(UUID.randomUUID().toString());
        appDatasourcePO.setAppId(appRegistrationPO.getId());

        boolean save = appDataSourceImpl.save(appDatasourcePO);

        return save ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 查询应用注册表
     *
     * @return
     */
    @Override
    public List<AppRegistrationVO> listAppRegistration() {

        return mapper.getData();
    }
}
