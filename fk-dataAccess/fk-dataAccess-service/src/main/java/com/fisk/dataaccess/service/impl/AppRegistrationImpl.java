package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.dto.PageDTO;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.dataaccess.dto.AppDataSourceDTO;
import com.fisk.dataaccess.dto.AppRegistrationDTO;
import com.fisk.dataaccess.dto.AppRegistrationEditDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.entity.AppDriveTypePO;
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
import java.sql.Wrapper;
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

    @Autowired
    private AppDriveTypeImpl appDriveTypeImpl;

    Date date = new Date(System.currentTimeMillis());

    /**
     * 添加应用
     *
     * @param appRegistrationDTO
     * @return
     */
    @Override
    @Transactional
    public ResultEnum addData(AppRegistrationDTO appRegistrationDTO) {

        // dto->po
        AppRegistrationPO appRegistrationPO = appRegistrationDTO.toEntity(AppRegistrationPO.class);

        // 保存tb_app_registration数据
/*        String appId = UUID.randomUUID().toString();
        appRegistrationPO.setId(appId);*/
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");

        Date date1 = new Date(System.currentTimeMillis());
        appRegistrationPO.setCreateTime(date1);
        appRegistrationPO.setUpdateTime(date1);
        appRegistrationPO.setDelFlag(1);

        /**
         * 数据保存需求更改: 添加应用的时候，相同的应用名称不可以再次添加
         */
        List<String> appNameList = baseMapper.getAppName();
        String appName = appRegistrationPO.getAppName();
        boolean contains = appNameList.contains(appName);
        if (contains) {
            throw new FkException(500, "当前数据已存在");
        }

        // 保存
        boolean save = this.save(appRegistrationPO);
        if (!save) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR, "数据保存失败");
        }


        AppDataSourcePO appDatasourcePO = appRegistrationDTO.getAppDatasourceDTO().toEntity(AppDataSourcePO.class);


        // 保存tb_app_datasource数据
        appDatasourcePO.setAppid(appRegistrationPO.getId());

        Date date2 = new Date(System.currentTimeMillis());
        appDatasourcePO.setCreateTime(date2);
        appDatasourcePO.setUpdateTime(date2);
        appDatasourcePO.setDelFlag(1);

//        boolean save = appDataSourceImpl.save(appDatasourcePO);

        int insert = appDataSourceMapper.insert(appDatasourcePO);

/*        // 保存tb_app_drivetype数据
        AppDriveTypePO appDriveTypePO = new AppDriveTypePO();
        appDriveTypePO.setId(1);
        appDriveTypePO.setName(appDatasourcePO.getDriveType());
        appDriveTypeImpl.save(appDriveTypePO);*/

        return insert > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 分页查询应用注册表
     *
     * @return
     */
    @Override
    public PageDTO<AppRegistrationDTO> listAppRegistration(String key, Integer page, Integer rows) {

        // 1.分页信息的健壮性处理
        page = Math.min(page, 100);  // 返回二者间较小的值,即当前页最大不超过100页,避免单词查询太多数据影响效率
        rows = Math.max(rows, 1);    // 每页至少1条

        Page<AppRegistrationPO> registrationPOPage1 = new Page<>(page, rows);

        boolean isKeyExists = StringUtils.isNoneBlank(key);
        query().like(isKeyExists, "app_name", key)
//                .or()
//                .eq(isKeyExists, "app_des", key)
//                .or()
//                .eq(isKeyExists, "app_type", key)
//                .or()
//                .eq(isKeyExists, "app_principal", key)
//                .or()
                .eq("del_flag", 1)// 未删除
                .page(registrationPOPage1);

        // 取出数据列表
        List<AppRegistrationPO> records1 = registrationPOPage1.getRecords();



        Page<AppRegistrationPO> registrationPOPage2 = new Page<>(page, rows);


        QueryWrapper<AppRegistrationPO> queryWrapper = new QueryWrapper<>();

        queryWrapper.like(isKeyExists, "app_name", key)
                .eq("del_flag", 1);// 未删除
        baseMapper.selectPage(registrationPOPage2, queryWrapper);
        List<AppRegistrationPO> records2 = registrationPOPage2.getRecords();
        PageDTO<AppRegistrationDTO> pageDTO = new PageDTO<>();
        pageDTO.setTotal(registrationPOPage1.getTotal());// 总条数
        long totalPage = (long) (records1.size() + rows - 1) / rows;// 总页数
        pageDTO.setTotalPage(registrationPOPage1.getPages());
        pageDTO.setItems(AppRegistrationDTO.convertEntityList(records2));

        return pageDTO;
    }

    /**
     * 应用注册-修改
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public ResultEnum updateAppRegistration(AppRegistrationEditDTO dto) {

        // 1.0前端应用注册传来的id
        long id = dto.getId();

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
        long appDataSid = appDataSourceImpl.query().eq("appid", id).one().getId();
        appDataSourcePO.setId(appDataSid);
        appDataSourcePO.setUpdateTime(date);
        int update = appDataSourceMapper.updateById(appDataSourcePO);


        return update > 0 ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;

    }

    /**
     * 删除应用注册
     *
     * @param id
     * @return
     */
    @Override
    public ResultEnum deleteAppRegistration(long id) {

        AppRegistrationPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 1.删除tb_app_registration表数据
        model.setDelFlag(0);
        boolean updateReg = this.updateById(model);
        if (!updateReg) {
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据更新失败");
        }

        // 2.删除tb_app_datasource表数据
        AppDataSourcePO appDataSourcePO = appDataSourceImpl.query().eq("appid", id).one();
        appDataSourcePO.setDelFlag(0);
        int updateData = appDataSourceMapper.updateById(appDataSourcePO);

        return updateData > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 根据是否为实时,查询应用名称集合
     * @param appType
     * @return
     */
    @Override
    public List<String> queryAppName(byte appType) {

        List<AppRegistrationPO> list = this.query()
                .eq("app_type", appType)
                .eq("del_flag",1)
                .list();

        List<String> appName = new ArrayList<>();
        for (AppRegistrationPO appRegistrationPO : list) {
            appName.add(appRegistrationPO.getAppName());
        }

        return appName;
    }

    /**
     * 根据id查询数据,用于数据回显
     * @param id
     * @return
     */
    @Override
    public AppRegistrationDTO getData(long id) {

        AppRegistrationPO registrationPO = this.query()
                .eq("id", id)
                .eq("del_flag",1)
                .one();
        AppRegistrationDTO appRegistrationDTO = new AppRegistrationDTO(registrationPO);
//        appRegistrationDTO.setCreateTime(registrationPO.getCreateTime());

        AppDataSourcePO appDataSourcePO = appDataSourceImpl.query()
                .eq("appid", id)
                .eq("del_flag",1)
                .one();
        AppDataSourceDTO appDataSourceDTO = new AppDataSourceDTO(appDataSourcePO);
        appRegistrationDTO.setAppDatasourceDTO(appDataSourceDTO);

        return appRegistrationDTO;
    }


    @Override
    public List<AppRegistrationDTO> getDescDate() {

        List<AppRegistrationPO> descDate = baseMapper.getDescDate();

        return AppRegistrationDTO.convertEntityList(descDate);
    }
}
