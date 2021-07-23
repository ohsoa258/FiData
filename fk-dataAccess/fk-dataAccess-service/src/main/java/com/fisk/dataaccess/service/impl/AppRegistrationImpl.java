package com.fisk.dataaccess.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.dto.PageDTO;
import com.fisk.common.mdc.TraceType;
import com.fisk.common.mdc.TraceTypeEnum;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.dataaccess.dto.*;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.entity.AppDriveTypePO;
import com.fisk.dataaccess.entity.AppRegistrationPO;
import com.fisk.dataaccess.map.AppDataSourceMap;
import com.fisk.dataaccess.map.AppRegistrationMap;
import com.fisk.dataaccess.mapper.AppDataSourceMapper;
import com.fisk.dataaccess.mapper.AppDriveTypeMapper;
import com.fisk.dataaccess.mapper.AppRegistrationMapper;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.task.client.PublishTaskClient;
import com.fisk.task.dto.atlas.AtlasEntityDTO;
import com.fisk.task.dto.atlas.AtlasEntityQueryDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lock
 */
@Service
@Slf4j
public class AppRegistrationImpl extends ServiceImpl<AppRegistrationMapper, AppRegistrationPO> implements IAppRegistration {

    @Resource
    private AppDataSourceMapper appDataSourceMapper;
    @Resource
    private AppRegistrationMapper mapper;
    @Resource
    private AppDataSourceImpl appDataSourceImpl;
    @Resource
    private AppDriveTypeImpl appDriveTypeImpl;
    @Resource
    private AppDriveTypeMapper appDriveTypeMapper;
    @Resource
    private PublishTaskClient publishTaskClient;
    @Resource
    UserHelper userHelper;


    /**
     * 添加应用
     *
     * @param appRegistrationDTO 请求参数
     * @return 返回值
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(AppRegistrationDTO appRegistrationDTO) {

        UserInfo userInfo = userHelper.getLoginUserInfo();
        Long userId = userInfo.id;

        // dto->po
        AppRegistrationPO po = appRegistrationDTO.toEntity(AppRegistrationPO.class);

        // 保存tb_app_registration数据
        po.setCreateUser(String.valueOf(userId));

        // 数据保存需求更改: 添加应用的时候，相同的应用名称不可以再次添加
        List<String> appNameList = baseMapper.getAppName();
        String appName = po.getAppName();
        boolean contains = appNameList.contains(appName);
        if (contains) {
            return ResultEnum.DATA_EXISTS;
        }

        // 保存
        boolean save = this.save(po);
        if (!save) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        AppDataSourcePO modelDataSource = appRegistrationDTO.getAppDatasourceDTO().toEntity(AppDataSourcePO.class);

        // 保存tb_app_datasource数据
        modelDataSource.setAppid(po.getId());
        modelDataSource.setCreateUser(String.valueOf(userId));

        int insert = appDataSourceMapper.insert(modelDataSource);
//        if (insert < 0) {
//            return ResultEnum.SAVE_DATA_ERROR;
//        }

        // 保存tb_app_drivetype数据
//        AppDriveTypePO po2 = new AppDriveTypePO();
//        po2.setId(po.getId());
//        po2.setName(po1.getDriveType());
//        boolean save2 = appDriveTypeImpl.save(po2);

/*        if (!save2) {
            throw new FkException(500, "保存tb_app_drivetype数据失败");
        }*/

        // TODO: atlas对接应用注册
        AtlasEntityQueryDTO atlasEntityQueryDTO = new AtlasEntityQueryDTO();

        atlasEntityQueryDTO.appId = "" + po.getId() + "";

        atlasEntityQueryDTO.userId = userId;
        ResultEntity<Object> task = publishTaskClient.publishBuildAtlasInstanceTask(atlasEntityQueryDTO);
        log.info("task:" + JSON.toJSONString(task));

//        System.out.println(task);


        return insert > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 分页查询
     *
     * @param key  搜索条件
     * @param page 当前页码
     * @param rows 每页显示条数
     * @return 返回值
     */
    @Override
    public PageDTO<AppRegistrationDTO> listAppRegistration(String key, Integer page, Integer rows) {

        Page<AppRegistrationPO> pageReg = new Page<>(page, rows);

        boolean isKeyExists = StringUtils.isNoneBlank(key);
        query().like(isKeyExists, "app_name", key)
                // 未删除
                .eq("del_flag", 1)
                .page(pageReg);

        // 分页封装
        Page<AppRegistrationPO> poPage = new Page<>(page, rows);


        QueryWrapper<AppRegistrationPO> queryWrapper = new QueryWrapper<>();


        // 查询数据
        queryWrapper.like(isKeyExists, "app_name", key)
                .eq("del_flag", 1)
                // 未删除
                .orderByDesc("create_time");
        baseMapper.selectPage(poPage, queryWrapper);

        List<AppRegistrationPO> records2 = poPage.getRecords();
        PageDTO<AppRegistrationDTO> pageDTO = new PageDTO<>();

        // 总条数
        pageDTO.setTotal(pageReg.getTotal());
        // 总页数
//        long totalPage = (long) (records1.size() + rows - 1) / rows;
        pageDTO.setTotalPage(pageReg.getPages());

        pageDTO.setItems(AppRegistrationMap.INSTANCES.listPoToDto(records2));


        return pageDTO;
    }

    /**
     * 应用注册-修改
     *
     * @param dto 请求参数
     * @return 返回值
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public ResultEnum updateAppRegistration(AppRegistrationEditDTO dto) {

        // 获取当前登陆人信息
        UserInfo userInfo = userHelper.getLoginUserInfo();
        Long userId = userInfo.id;

        // 1.0前端应用注册传来的id
        long id = dto.getId();

        // 1.1非空判断
        AppRegistrationPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 1.2dto->po
        AppRegistrationPO po = dto.toEntity(AppRegistrationPO.class);

        // 1.3修改tb_app_registration数据
        po.setUpdateUser(String.valueOf(userId));
        boolean edit = this.updateById(po);
        if (!edit) {
            return ResultEnum.UPDATE_DATA_ERROR;
        }

        // 2.0修改关联表数据(tb_app_datasource)

        // 2.1dto->po
        AppDataSourceDTO appDatasourceDTO = dto.getAppDatasourceDTO();

        AppDataSourcePO modelDataSource = appDatasourceDTO.toEntity(AppDataSourcePO.class);

        // 2.2修改数据
        long appDataSid = appDataSourceImpl.query()
                .eq("appid", id)
                .one()
                .getId();
        modelDataSource.setId(appDataSid);

        modelDataSource.setAppid(id);
        // 更新人
        modelDataSource.updateUser = String.valueOf(userId);

        return appDataSourceMapper.updateById(modelDataSource) > 0 ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;
    }

    /**
     * 删除应用注册
     *
     * @param id 请求参数
     * @return 返回值
     */
    @Override
    public ResultEnum deleteAppRegistration(long id) {

        AppRegistrationPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        // 1.删除tb_app_registration表数据
        int deleteReg = mapper.deleteByIdWithFill(model);
        if (deleteReg < 0) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        // 2.删除tb_app_datasource表数据
        AppDataSourcePO modelDataSource = appDataSourceImpl.query()
                .eq("appid", id)
                .one();

        return appDataSourceMapper.deleteByIdWithFill(modelDataSource) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 查询所有应用名称(实时  非实时)
     *
     * @return 返回值
     */
    @Override
    public List<AppNameDTO> queryAppName() {

        // 查询所有应用名称
        List<AppRegistrationPO> list = this.query()
                .eq("del_flag", 1)
                .list();
        List<AppNameDTO> listAppName = new ArrayList<>();
        for (AppRegistrationPO po : list) {

            AppNameDTO appNameDTO = new AppNameDTO();
            String appName = po.getAppName();
            appNameDTO.setAppName(appName);
            appNameDTO.setAppType((byte) po.getAppType());

            listAppName.add(appNameDTO);
        }

        return listAppName;
    }


    /**
     * 根据id查询数据,用于数据回显
     *
     * @param id 请求参数
     * @return 返回值
     */
    @Override
    public AppRegistrationDTO getData(long id) {

        AppRegistrationPO modelReg = this.query()
                .eq("id", id)
                .eq("del_flag", 1)
                .one();
        AppRegistrationDTO appRegistrationDTO = AppRegistrationMap.INSTANCES.poToDto(modelReg);


        AppDataSourcePO modelDataSource = appDataSourceImpl.query()
                .eq("appid", id)
                .eq("del_flag", 1)
                .one();
        AppDataSourceDTO appDataSourceDTO = AppDataSourceMap.INSTANCES.poToDto(modelDataSource);
        appRegistrationDTO.setAppDatasourceDTO(appDataSourceDTO);

        return appRegistrationDTO;
    }


    /**
     * @return 返回值
     */
    @Override
    public List<AppRegistrationDTO> getDescDate() {

        // 按时间倒叙,查询top10的数据
        List<AppRegistrationPO> descDate = baseMapper.getDescDate();

        return AppRegistrationMap.INSTANCES.listPoToDto(descDate);
    }

    /**
     * 查询所有非实时应用名称
     *
     * @return 返回值
     */
    @Override
    public List<AppNameDTO> queryNoneRealTimeAppName() {

        List<AppRegistrationPO> list = this.query()
                .eq("del_flag", 1)
                .eq("app_type", 1)
                .list();
        List<AppNameDTO> listAppName = new ArrayList<>();
        for (AppRegistrationPO po : list) {

            AppNameDTO appNameDTO = new AppNameDTO();
            String appName = po.getAppName();
            appNameDTO.setAppName(appName);
            appNameDTO.setAppType((byte) 1);

            listAppName.add(appNameDTO);
        }

        return listAppName;
    }

    @Override
    public List<AppDriveTypeDTO> getDriveType() {

        List<AppDriveTypePO> list = appDriveTypeMapper.listData();

        return AppDriveTypeDTO.convertEntityList(list);
    }


    @TraceType(type = TraceTypeEnum.DATAACCESS_GET_ATLAS_ENTITY)
    @Override
    public AtlasEntityDTO getAtlasEntity(long id) {

        AtlasEntityDTO dto = null;
        try {
            dto = new AtlasEntityDTO();

            AppRegistrationPO modelReg = this.query().eq("id", id)
                    .eq("del_flag", 1)
                    .one();

            AppDataSourcePO modelDataSource = appDataSourceImpl.query().eq("id", id)
                    .eq("del_flag", 1)
                    .one();

            dto.sendTime = LocalDateTime.now();
            dto.appName = modelReg.appName;
            dto.createUser = modelReg.getCreateUser();
            dto.appDes = modelReg.getAppDes();

            String driveType = "mysql";
            if (driveType.equalsIgnoreCase(modelDataSource.getDriveType())) {
                dto.driveType = "MySQL";
            } else {
                dto.driveType = modelDataSource.getDriveType();
            }
            dto.host = modelDataSource.getHost();
            dto.port = modelDataSource.getPort();
            dto.dbName = modelDataSource.getDbName();

        } catch (Exception e) {
            log.error("{}方法执行失败: ", e);
//            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }

        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultEnum addAtlasInstanceIdAndDbId(long appid, String atlasInstanceId, String atlasDbId) {

        AppRegistrationPO modelReg = this.query().eq("id", appid)
                .eq("del_flag", 1)
                .one();
        if (modelReg == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }
        modelReg.atlasInstanceId = atlasInstanceId;
//        model.delFlag = 1;
        // 保存tb_app_registration
        boolean update = this.updateById(modelReg);
        if (!update) {
            return ResultEnum.SAVE_DATA_ERROR;
        }

        AppDataSourcePO modelData = appDataSourceImpl.query()
                .eq("appid", appid)
                .eq("del_flag", 1)
                .one();
        if (modelData == null) {
            return ResultEnum.SAVE_DATA_ERROR;
        }
        modelData.atlasDbId = atlasDbId;
        // 保存tb_app_datasource
        boolean updateById = appDataSourceImpl.updateById(modelData);

        return updateById ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

}
