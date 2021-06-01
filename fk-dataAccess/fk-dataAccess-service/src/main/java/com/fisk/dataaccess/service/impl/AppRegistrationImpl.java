package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.dto.PageDTO;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.AppDataSourceDTO;
import com.fisk.dataaccess.dto.AppRegistrationDTO;
import com.fisk.dataaccess.dto.AppRegistrationEditDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.entity.AppRegistrationPO;
import com.fisk.dataaccess.mapper.AppDataSourceMapper;
import com.fisk.dataaccess.mapper.AppRegistrationMapper;
import com.fisk.dataaccess.service.IAppRegistration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
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

    Date date = new Date(System.currentTimeMillis());

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
        appDatasourcePO.setAppid(appRegistrationPO.getId());

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

        Page<AppRegistrationPO> registrationPOPage = new Page<>(page, rows);

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

        PageDTO<AppRegistrationDTO> pageDTO = new PageDTO<>();
        pageDTO.setTotal((long)records.size());
        long totalPage = (long) (records.size() + rows - 1) / rows;
        pageDTO.setTotalPage(totalPage);
        pageDTO.setItems(AppRegistrationDTO.convertEntityList(records));

        return pageDTO;
    }

    /**
     * 应用注册-修改
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public ResultEnum updateAppRegistration(AppRegistrationEditDTO dto) {

        // 1.0前端应用注册传来的id
        String id = dto.getId();

        // 1.1非空判断
        AppRegistrationPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 1.2dto->po
        AppRegistrationPO appRegistrationPO = dto.toEntity(AppRegistrationPO.class);

        // 1.3修改主表数据
        appRegistrationPO.setUpdateTime(date);
        boolean edit = this.updateById(appRegistrationPO);
        if (!edit) {
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据更新失败");
        }

        // 2.0修改关联表数据(tb_app_datasource)

        // 2.1dto->po
        AppDataSourceDTO appDatasourceDTO = dto.getAppDatasourceDTO();

        AppDataSourcePO appDataSourcePO = appDatasourceDTO.toEntity(AppDataSourcePO.class);

        // 2.2修改数据
//        String appDataSid = appDataSourceImpl.query().eq("appid", id).one().getId();
        int update = appDataSourceMapper.updateById(appDataSourcePO);


        return update>0?ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;

    }
}
