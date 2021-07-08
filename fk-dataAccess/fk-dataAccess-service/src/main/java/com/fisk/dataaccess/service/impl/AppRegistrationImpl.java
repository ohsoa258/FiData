package com.fisk.dataaccess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.auth.dto.UserDetail;
import com.fisk.auth.utils.UserContext;
import com.fisk.common.dto.PageDTO;
import com.fisk.common.exception.FkException;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.user.UserHelper;
import com.fisk.common.user.UserInfo;
import com.fisk.dataaccess.dto.AppDataSourceDTO;
import com.fisk.dataaccess.dto.AppNameDTO;
import com.fisk.dataaccess.dto.AppRegistrationDTO;
import com.fisk.dataaccess.dto.AppRegistrationEditDTO;
import com.fisk.dataaccess.entity.AppDataSourcePO;
import com.fisk.dataaccess.entity.AppDriveTypePO;
import com.fisk.dataaccess.entity.AppRegistrationPO;
import com.fisk.dataaccess.mapper.AppDataSourceMapper;
import com.fisk.dataaccess.mapper.AppRegistrationMapper;
import com.fisk.dataaccess.service.IAppRegistration;
import com.fisk.task.dto.daconfig.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Lock
 */
@Service
public class AppRegistrationImpl extends ServiceImpl<AppRegistrationMapper, AppRegistrationPO> implements IAppRegistration {

    @Resource
    private AppDataSourceMapper appDataSourceMapper;

    @Resource
    private AppDataSourceImpl appDataSourceImpl;

    @Resource
    private AppDriveTypeImpl appDriveTypeImpl;

    private Date date = new Date(System.currentTimeMillis());

    /**
     * 添加应用
     *
     * @param appRegistrationDTO 请求参数
     * @return 返回值
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultEnum addData(AppRegistrationDTO appRegistrationDTO) {

        // dto->po
        AppRegistrationPO po = appRegistrationDTO.toEntity(AppRegistrationPO.class);


        // 保存tb_app_registration数据
        Date date1 = new Date(System.currentTimeMillis());
        po.setCreateTime(date1);
        po.setUpdateTime(date1);
        po.setDelFlag(1);

        // 数据保存需求更改: 添加应用的时候，相同的应用名称不可以再次添加
        List<String> appNameList = baseMapper.getAppName();
        String appName = po.getAppName();
        boolean contains = appNameList.contains(appName);
        if (contains) {
            throw new FkException(ResultEnum.DATA_EXISTS);
        }

        // 保存
        boolean save = this.save(po);
        if (!save) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }


        AppDataSourcePO po1 = appRegistrationDTO.getAppDatasourceDTO().toEntity(AppDataSourcePO.class);


        // 保存tb_app_datasource数据
        po1.setAppid(po.getId());

        Date date2 = new Date(System.currentTimeMillis());
        po1.setCreateTime(date2);
        po1.setUpdateTime(date2);
        po1.setDelFlag(1);

        int insert = appDataSourceMapper.insert(po1);
        if (insert < 0) {
            throw new FkException(ResultEnum.SAVE_DATA_ERROR);
        }

        // 保存tb_app_drivetype数据
        AppDriveTypePO po2 = new AppDriveTypePO();
        po2.setId(po.getId());
        po2.setName(po1.getDriveType());
        boolean save2 = appDriveTypeImpl.save(po2);

/*        if (!save2) {
            throw new FkException(500, "保存tb_app_drivetype数据失败");
        }*/

//        return insert > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        return save2 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
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

        // 1.分页信息的健壮性处理
        // 返回二者间较小的值,即当前页最大不超过100页,避免单词查询太多数据影响效率
        page = Math.min(page, 100);
        // 每页至少1条
        rows = Math.max(rows, 1);

        Page<AppRegistrationPO> page1 = new Page<>(page, rows);

        boolean isKeyExists = StringUtils.isNoneBlank(key);
        query().like(isKeyExists, "app_name", key)
//                .or()
//                .eq(isKeyExists, "app_des", key)
//                .or()
//                .eq(isKeyExists, "app_type", key)
//                .or()
//                .eq(isKeyExists, "app_principal", key)
//                .or()
                // 未删除
                .eq("del_flag", 1)
                .page(page1);

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
        pageDTO.setTotal(page1.getTotal());
        // 总页数
//        long totalPage = (long) (records1.size() + rows - 1) / rows;
        pageDTO.setTotalPage(page1.getPages());
        pageDTO.setItems(AppRegistrationDTO.convertEntityList(records2));

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

        // 1.0前端应用注册传来的id
        long id = dto.getId();

        // 1.1非空判断
        AppRegistrationPO model = this.getById(id);
        if (model == null) {
            return ResultEnum.DATA_NOTEXISTS;
        }

        // 1.2dto->po
        AppRegistrationPO po = dto.toEntity(AppRegistrationPO.class);

        // 1.3修改主表数据
        po.setUpdateTime(date);
        po.setDelFlag(1);
        boolean edit = this.updateById(po);
        if (!edit) {
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据更新失败");
        }

        // 2.0修改关联表数据(tb_app_datasource)

        // 2.1dto->po
        AppDataSourceDTO appDatasourceDTO = dto.getAppDatasourceDTO();

        AppDataSourcePO dpo = appDatasourceDTO.toEntity(AppDataSourcePO.class);

        // 2.2修改数据
        long appDataSid = appDataSourceImpl.query().eq("appid", id).one().getId();
        dpo.setId(appDataSid);

        dpo.setAppid(id);

        Date date1 = new Date(System.currentTimeMillis());
        dpo.setUpdateTime(date1);
        dpo.setDelFlag(1);
        int update = appDataSourceMapper.updateById(dpo);


        return update > 0 ? ResultEnum.SUCCESS : ResultEnum.UPDATE_DATA_ERROR;

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
        model.setDelFlag(0);
        boolean updateReg = this.updateById(model);
        if (!updateReg) {
            throw new FkException(ResultEnum.UPDATE_DATA_ERROR, "数据更新失败");
        }

        // 2.删除tb_app_datasource表数据
        AppDataSourcePO po1 = appDataSourceImpl.query().eq("appid", id).one();
        po1.setDelFlag(0);
        int updateData = appDataSourceMapper.updateById(po1);

        return updateData > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    /**
     * 查询所有应用名称(实时  非实时)
     *
     * @return 返回值
     */
    @Override
    public List<AppNameDTO> queryAppName() {

        List<AppRegistrationPO> list = this.query()
                .eq("del_flag", 1)
                .list();
        List<AppNameDTO> list1 = new ArrayList<>();
        for (AppRegistrationPO po : list) {

            AppNameDTO appNameDTO = new AppNameDTO();
            String appName = po.getAppName();
            appNameDTO.setAppName(appName);
            appNameDTO.setAppType((byte) po.getAppType());

            list1.add(appNameDTO);
        }

        return list1;
    }


    /**
     * 根据id查询数据,用于数据回显
     *
     * @param id 请求参数
     * @return 返回值
     */
    @Override
    public AppRegistrationDTO getData(long id) {

        AppRegistrationPO po1 = this.query()
                .eq("id", id)
                .eq("del_flag", 1)
                .one();
        AppRegistrationDTO appRegistrationDTO = new AppRegistrationDTO(po1);

        AppDataSourcePO po2 = appDataSourceImpl.query()
                .eq("appid", id)
                .eq("del_flag", 1)
                .one();
        AppDataSourceDTO appDataSourceDTO = new AppDataSourceDTO(po2);
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

        return AppRegistrationDTO.convertEntityList(descDate);
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
        List<AppNameDTO> list1 = new ArrayList<>();
        for (AppRegistrationPO po : list) {

            AppNameDTO appNameDTO = new AppNameDTO();
            String appName = po.getAppName();
            appNameDTO.setAppName(appName);
            appNameDTO.setAppType((byte) 1);

            list1.add(appNameDTO);
        }

        return list1;
    }

    @Override
    public ResultEnum dataAccessConfig(long id) {

        DataAccessConfigDTO dto = new DataAccessConfigDTO();

        // app组配置
        GroupConfig groupConfig = dto.getGroupConfig();

        //任务组配置
        TaskGroupConfig taskGroupConfig = dto.getTaskGroupConfig();

        // 数据源jdbc配置
        DataSourceConfig sourceDsConfig = dto.getSourceDsConfig();

        // 目标源jdbc连接
        DataSourceConfig targetDsConfig = dto.getTargetDsConfig();

        // 表及表sql
        ProcessorConfig processorConfig = dto.getProcessorConfig();

        // 1.app组配置
        // select * from tb_app_registration where id=id and del_flag=1;
        AppRegistrationPO rpo = this.query()
                .eq("id", id)
                .eq("del_flag", 1)
                .one();
        if (rpo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        groupConfig.setAppName(rpo.getAppName());
        groupConfig.setAppDetails(rpo.getAppDes());
        // TODO: 缺失字段
        groupConfig.setNewApp(false);

        // 2.任务组配置
        taskGroupConfig.setAppName(rpo.getAppName());
        taskGroupConfig.setAppDetails(rpo.getAppDes());

        //3.数据源jdbc配置
        AppDataSourcePO dpo = appDataSourceImpl.query()
                .eq("appid", id)
                .eq("del_flag", 1)
                .one();
        if (dpo == null) {
            throw new FkException(ResultEnum.DATA_NOTEXISTS);
        }
        sourceDsConfig.setJdbcStr(dpo.getConnectStr());
//        sourceDsConfig.setType(); // 先硬编码
        sourceDsConfig.setUser(dpo.getConnectAccount());
        sourceDsConfig.setPassword(dpo.getConnectPwd());

        // 4.目标源jdbc连接

        // 5.表及表sql

        return null;
    }
}
