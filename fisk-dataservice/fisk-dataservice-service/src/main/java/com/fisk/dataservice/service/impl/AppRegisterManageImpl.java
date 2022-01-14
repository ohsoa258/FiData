package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.constants.FilterSqlConstants;
import com.fisk.common.filter.dto.FilterFieldDTO;
import com.fisk.common.filter.method.GenerateCondition;
import com.fisk.common.filter.method.GetMetadata;
import com.fisk.common.response.ResultEnum;
import com.fisk.common.utils.EnCryptUtils;
import com.fisk.dataservice.dto.app.AppRegisterPageDTO;
import com.fisk.dataservice.dto.app.AppRegisterQueryDTO;
import com.fisk.dataservice.dto.app.AppRegisterDTO;
import com.fisk.dataservice.dto.app.AppRegisterEditDTO;
import com.fisk.dataservice.dto.app.AppApiSubQueryDTO;
import com.fisk.dataservice.dto.app.AppApiParmQueryDTO;
import com.fisk.dataservice.dto.app.AppApiSubDTO;
import com.fisk.dataservice.dto.app.AppPwdResetDTO;
import com.fisk.dataservice.dto.app.AppApiBuiltinParmEditDTO;
import com.fisk.dataservice.entity.*;
import com.fisk.dataservice.map.ApiBuiltinParmMap;
import com.fisk.dataservice.map.ApiParmMap;
import com.fisk.dataservice.map.AppApiMap;
import com.fisk.dataservice.map.AppRegisterMap;
import com.fisk.dataservice.mapper.*;
import com.fisk.dataservice.service.IAppRegisterManageService;
import com.fisk.dataservice.vo.app.AppApiParmVO;
import com.fisk.dataservice.vo.app.AppApiSubVO;
import com.fisk.dataservice.vo.app.AppRegisterVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 应用接口实现类
 *
 * @author dick
 */
@Service
public class AppRegisterManageImpl extends ServiceImpl<AppRegisterMapper, AppConfigPO> implements IAppRegisterManageService {

    @Resource
    private GetMetadata getMetadata;

    @Resource
    private GenerateCondition generateCondition;

    @Resource
    private AppApiMapper appApiMapper;

    @Resource
    private ApiRegisterMapper apiRegisterMapper;

    @Resource
    private ApiParmMapper apiParmMapper;

    @Resource
    private ApiBuiltinParmMapper apiBuiltinParmMapper;

    @Resource
    private ApiBuiltinParmManageImpl apiBuiltinParmImpl;

    @Override
    public List<FilterFieldDTO> getColumn() {
        return getMetadata.getMetadataList(
                "dmp_dataservice_db",
                "tb_app_config",
                "",
                FilterSqlConstants.DS_APP_REGISTRATION_SQL);
    }

    @Override
    public Page<AppRegisterVO> pageFilter(AppRegisterQueryDTO query) {
        StringBuilder querySql = new StringBuilder();
        // 拼接原生筛选条件
        querySql.append(generateCondition.getCondition(query.dto));
        AppRegisterPageDTO data = new AppRegisterPageDTO();
        data.page = query.page;
        // 筛选器左边的模糊搜索查询SQL拼接
        data.where = querySql.toString();

        return baseMapper.filter(query.page, data);
    }

    @Override
    public Page<AppRegisterVO> getAll(Page<AppRegisterVO> page) {
        return baseMapper.getAll(page);
    }

    @Override
    public ResultEnum addData(AppRegisterDTO dto) {
        QueryWrapper<AppConfigPO> queryWrapper = new QueryWrapper<>();
        // and(appName = '' or appAccount = '')
        queryWrapper.lambda().and(wq -> wq.eq(AppConfigPO::getAppName, dto.appName).
                        or().eq(AppConfigPO::getAppAccount, dto.appAccount))
                .eq(AppConfigPO::getDelFlag, 1);
        List<AppConfigPO> selectList = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(selectList)) {
            Optional<AppConfigPO> appConfigOptional = selectList.stream().filter(item -> item.getAppName().equals(dto.appName)).findFirst();
            if (appConfigOptional.isPresent()) {
                return ResultEnum.DS_APP_NAME_EXISTS;
            }
            appConfigOptional = selectList.stream().filter(item -> item.getAppAccount().equals(dto.appAccount)).findFirst();
            if (appConfigOptional.isPresent()) {
                return ResultEnum.DS_APP_ACCOUNT_EXISTS;
            }
        }
        AppConfigPO model = AppRegisterMap.INSTANCES.dtoToPo(dto);
        byte[] base64Encrypt = EnCryptUtils.base64Encrypt(dto.appPassword);
        model.setAppPassword(new String(base64Encrypt));
        return baseMapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum editData(AppRegisterEditDTO dto) {
        AppConfigPO model = baseMapper.selectById(dto.id);
        if (model == null) {
            return ResultEnum.DS_APP_EXISTS;
        }
        QueryWrapper<AppConfigPO> queryWrapper = new QueryWrapper<>();
        // and(appName = '' or appAccount = '')
        queryWrapper.lambda().and(wq -> wq.eq(AppConfigPO::getAppName, dto.appName).
                        or().eq(AppConfigPO::getAppAccount, dto.appAccount))
                .eq(AppConfigPO::getDelFlag, 1)
                .ne(AppConfigPO::getId, dto.id);
        List<AppConfigPO> selectList = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(selectList)) {
            Optional<AppConfigPO> appConfigOptional = selectList.stream().filter(item -> item.getAppName().equals(dto.appName)).findFirst();
            if (appConfigOptional.isPresent()) {
                return ResultEnum.DS_APP_NAME_EXISTS;
            }
            appConfigOptional = selectList.stream().filter(item -> item.getAppAccount().equals(dto.appAccount)).findFirst();
            if (appConfigOptional.isPresent()) {
                return ResultEnum.DS_APP_ACCOUNT_EXISTS;
            }
        }
        AppRegisterMap.INSTANCES.editDtoToPo(dto, model);
        return baseMapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum deleteData(int id) {
        AppConfigPO model = baseMapper.selectById(id);
        if (model == null) {
            return ResultEnum.DS_APP_EXISTS;
        }
        // 查询应用下是否存在api
        QueryWrapper<AppApiPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AppApiPO::getAppId, id).eq(AppApiPO::getApiState, 1)
                .eq(AppApiPO::getDelFlag, 1);
        AppApiPO data = appApiMapper.selectOne(queryWrapper);
        if (data == null) {
            // 该应用下没有启用的api，可以直接删除
            return baseMapper.deleteByIdWithFill(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        } else {
            // 应用下有已启用的api,必须先禁用api
            return ResultEnum.DS_APP_API_EXISTS;
        }
    }

    @Override
    public Page<AppApiSubVO> getSubscribeAll(AppApiSubQueryDTO dto) {
        return appApiMapper.getSubscribeAll(dto.page, dto);
    }

    @Override
    public ResultEnum appSubscribe(AppApiSubDTO dto) {
        ApiConfigPO apiModel = apiRegisterMapper.selectById(dto.apiId);
        if (apiModel == null)
            return ResultEnum.DS_API_EXISTS;
        AppConfigPO appModel = baseMapper.selectById(dto.appId);
        if (appModel == null)
            return ResultEnum.DS_APP_EXISTS;

        // 根据应用id和APIID查询是否存在订阅记录
        QueryWrapper<AppApiPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AppApiPO::getAppId, dto.appId).eq(AppApiPO::getApiId, dto.apiId)
                .eq(AppApiPO::getDelFlag, 1);
        ;
        AppApiPO data = appApiMapper.selectOne(queryWrapper);
        if (data != null) {
            // 存在则修改状态
            data.setApiState(data.apiState = dto.apiState.getValue());
            return appApiMapper.updateById(data) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        } else {
            // 不存在则新增
            AppApiPO model = AppApiMap.INSTANCES.dtoToPo(dto);
            return appApiMapper.insert(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        }
    }

    @Override
    public ResultEnum resetPwd(AppPwdResetDTO dto) {
        AppConfigPO model = baseMapper.selectById(dto.appId);
        if (model == null)
            return ResultEnum.DS_APP_EXISTS;
        if (dto.appPassword.isEmpty())
            return ResultEnum.DS_APP_PWD_NOTNULL;

        byte[] base64Encrypt = EnCryptUtils.base64Encrypt(dto.appPassword);
        model.setAppPassword(new String(base64Encrypt));
        return baseMapper.updateById(model) > 0 ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
    }

    @Override
    public ResultEnum createDoc(Integer appId) {
        return null;
    }

    @Override
    public List<AppApiParmVO> getParmAll(AppApiParmQueryDTO dto) {
        List<AppApiParmVO> appApiParmList = new ArrayList<>();
        QueryWrapper<ParmConfigPO> query = new QueryWrapper<>();
        query.lambda()
                .eq(ParmConfigPO::getApiId, dto.apiId)
                .eq(ParmConfigPO::getDelFlag, 1);
        List<ParmConfigPO> selectList = apiParmMapper.selectList(query);
        if (CollectionUtils.isNotEmpty(selectList)) {
            // 查询已设置内置参数的parm
            QueryWrapper<BuiltinParmPO> builtinParmQuery = new QueryWrapper<>();
            builtinParmQuery.lambda()
                    .eq(BuiltinParmPO::getApiId, dto.apiId)
                    .eq(BuiltinParmPO::getAppId, dto.appId)
                    .eq(BuiltinParmPO::getDelFlag, 1);
            List<BuiltinParmPO> builtinParmtList = apiBuiltinParmMapper.selectList(builtinParmQuery);
            if (CollectionUtils.isNotEmpty(builtinParmtList)) {
                for (ParmConfigPO parmConfigPO : selectList) {
                    Optional<BuiltinParmPO> builtinParmOptional = builtinParmtList.stream().filter(item -> item.getParmId()==parmConfigPO.id).findFirst();
                    if (builtinParmOptional.isPresent()) {
                        // 存在
                        BuiltinParmPO builtinParm = builtinParmOptional.get();
                        parmConfigPO.setParmValue(builtinParm.parmValue);
                        parmConfigPO.setParmDesc(builtinParm.parmDesc);
                        parmConfigPO.setParmIsbuiltin(builtinParm.parmIsbuiltin);
                    }
                }
            }
            appApiParmList = ApiParmMap.INSTANCES.listPoToVo(selectList);
        }
        return appApiParmList;
    }

    @Override
    public ResultEnum setParm(AppApiBuiltinParmEditDTO dto) {
        ApiConfigPO apiModel = apiRegisterMapper.selectById(dto.apiId);
        if (apiModel == null)
            return ResultEnum.DS_API_EXISTS;
        AppConfigPO appModel = baseMapper.selectById(dto.appId);
        if (appModel == null)
            return ResultEnum.DS_APP_EXISTS;

        // 删除此应用API下的所有内置参数，再新增
        int updateCount = apiBuiltinParmMapper.updateBySearch(dto.appId, dto.apiId);

        List<BuiltinParmPO> builtinParmList = ApiBuiltinParmMap.INSTANCES.listDtoToPo(dto.parmList);
        if (CollectionUtils.isNotEmpty(builtinParmList)) {
            return apiBuiltinParmImpl.saveBatch(builtinParmList) ? ResultEnum.SUCCESS : ResultEnum.SAVE_DATA_ERROR;
        }
        return ResultEnum.SAVE_DATA_ERROR;
    }

}
