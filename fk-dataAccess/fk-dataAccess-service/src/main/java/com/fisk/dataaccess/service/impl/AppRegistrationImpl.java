package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.dto.PageDTO;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.vo.PageVO;
import com.fisk.dataaccess.dto.AppDataSourceDTO;
import com.fisk.dataaccess.dto.AppRegistrationDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.entity.AppRegistrationPO;
import com.fisk.dataaccess.mapper.AppDataSourceMapper;
import com.fisk.dataaccess.mapper.AppRegistrationMapper;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.dataaccess.vo.AppDataSourceVO;
import com.fisk.dataaccess.vo.AppRegistrationVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    @Resource
    private AppDataSourceMapper appDataSourceMapper;

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
        String appId = UUID.randomUUID().toString();
        appRegistrationPO.setId(appId);
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");

        Date date1 = new Date(System.currentTimeMillis());
        appRegistrationPO.setCreateTime(date1);
        appRegistrationPO.setUpdateTime(date1);
        // 保存应用注册表数据
        boolean save1 = this.save(appRegistrationPO);



        AppDataSourcePO appDatasourcePO = appRegistrationDTO.getAppDatasourceDTO().toEntity(AppDataSourcePO.class);

        appDatasourcePO.setId(UUID.randomUUID().toString());
        appDatasourcePO.setAppId(appRegistrationPO.getId());

        Date date2 = new Date(System.currentTimeMillis());
        appDatasourcePO.setCreateTime(date2);
        appDatasourcePO.setUpdateTime(date2);

//        boolean save = appDataSourceImpl.save(appDatasourcePO);

        int insert = appDataSourceMapper.insert(appDatasourcePO);

        return insert>0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 分页查询应用注册表
     *
     * @return
     */
    @Override
    public PageDTO<AppRegistrationDTO> listAppRegistration(String key,Integer page,Integer rows) {

        // 1.分页信息的健壮性处理
        page = Math.min(page, 100);  // 返回二者间较小的值,即当前页最大不超过100页,避免单词查询太多数据影响效率
        rows = Math.max(rows, 5);    // 每页至少5条

        IPage<AppRegistrationPO> registrationPOPage = new Page<>(page, rows);

        boolean isKeyExists = StringUtils.isNoneBlank(key);

        this.query().like(isKeyExists, "app_name", key)
                .or()
                .eq(isKeyExists, "app_des", key)
                .or()
                .eq(isKeyExists,"app_type",key)
                .or()
                .eq(isKeyExists,"app_principal",key)
                .page(registrationPOPage);

        // 取出数据列表
        List<AppRegistrationPO> records = registrationPOPage.getRecords();

        return new PageDTO<>(
                registrationPOPage.getTotal(), // 总条数
                registrationPOPage.getPages(),  // 总页数
                AppRegistrationDTO.convertEntityList(records) // 当前页数据
        );
    }
}
