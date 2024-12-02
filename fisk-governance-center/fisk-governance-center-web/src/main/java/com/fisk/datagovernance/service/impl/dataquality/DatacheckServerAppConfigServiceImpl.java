package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.constants.FilterSqlConstants;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.EnCryptUtils;
import com.fisk.common.framework.redis.RedisKeyEnum;
import com.fisk.common.framework.redis.RedisUtil;
import com.fisk.common.service.pageFilter.dto.FilterFieldDTO;
import com.fisk.common.service.pageFilter.dto.MetaDataConfigDTO;
import com.fisk.common.service.pageFilter.utils.GenerateCondition;
import com.fisk.common.service.pageFilter.utils.GetMetadata;
import com.fisk.datagovernance.dto.dataquality.datacheck.*;
import com.fisk.datagovernance.entity.dataquality.DatacheckServerAppConfigPO;
import com.fisk.datagovernance.map.dataquality.DatacheckServerAppConfigMap;
import com.fisk.datagovernance.mapper.dataquality.DatacheckServerAppConfigMapper;
import com.fisk.datagovernance.service.dataquality.DatacheckServerAppConfigService;
import com.fisk.datagovernance.vo.dataquality.datacheck.AppRegisterVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service("datacheckServerAppConfigService")
public class DatacheckServerAppConfigServiceImpl extends ServiceImpl<DatacheckServerAppConfigMapper, DatacheckServerAppConfigPO> implements DatacheckServerAppConfigService {


    @Resource
    private GenerateCondition generateCondition;
    @Resource
    RedisUtil redisUtil;

    @Value("${spring.datasource.url}")
    public String url;

    @Value("${spring.datasource.username}")
    public String username;

    @Value("${spring.datasource.password}")
    public String password;

    @Value("${spring.datasource.driver-class-name}")
    public String driver;

    @Resource
    private GetMetadata getMetadata;
    @Override
    public Page<AppRegisterVO> pageFilter(AppRegisterQueryDTO query) {
        StringBuilder querySql = new StringBuilder();
        // 拼接原生筛选条件
        querySql.append(generateCondition.getCondition(query.dto));
        AppRegisterPageDTO data = new AppRegisterPageDTO();
        data.page = query.page;
        // 筛选器左边的模糊搜索查询SQL拼接
        data.where = querySql.toString();

        int totalCount = 0;
        Page<AppRegisterVO> registerVOPage = baseMapper.filter(query.page, data);
//        if (registerVOPage != null && CollectionUtils.isNotEmpty(registerVOPage.getRecords())) {
//            // 查询应用下的API个数
//            List<AppServiceCountVO> appServiceCount = appApiMapper.getApiAppServiceCount();
//            for (AppRegisterVO appRegisterVO : registerVOPage.getRecords()) {
//                if (CollectionUtils.isNotEmpty(appServiceCount)) {
//                    AppServiceCountVO appServiceCountVO = appServiceCount.stream().filter(k -> k.getAppId() == appRegisterVO.getId()).findFirst().orElse(null);
//                    if (appServiceCountVO != null) {
//                        appRegisterVO.setItemCount(appServiceCountVO.getCount());
//                    }
//                }
//            }
//            if (CollectionUtils.isNotEmpty(appServiceCount)) {
//                totalCount = appServiceCount.stream().collect(Collectors.summingInt(AppServiceCountVO::getCount));
//                registerVOPage.getRecords().get(0).setTotalCount(totalCount);
//            }
//        }
        return registerVOPage;
    }

    @Override
    public ResultEnum addData(AppRegisterDTO dto) {
        QueryWrapper<DatacheckServerAppConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().and(wq -> wq.eq(DatacheckServerAppConfigPO::getAppName, dto.getAppName()).
                        or().eq(DatacheckServerAppConfigPO::getAppAccount, dto.getAppAccount()))
                .eq(DatacheckServerAppConfigPO::getDelFlag, 1);
        List<DatacheckServerAppConfigPO> selectList = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(selectList)) {
            Optional<DatacheckServerAppConfigPO> appConfigOptional = selectList.stream().filter(item -> item.getAppName().equals(dto.getAppName())).findFirst();
            if (StringUtils.isNotEmpty(dto.getAppName()) && appConfigOptional.isPresent()) {
                return ResultEnum.DS_APP_NAME_EXISTS;
            }
            appConfigOptional = selectList.stream().filter(item -> item.getAppAccount().equals(dto.getAppAccount())).findFirst();
            if (StringUtils.isNotEmpty(dto.getAppAccount()) && appConfigOptional.isPresent()) {
                return ResultEnum.DS_APP_ACCOUNT_EXISTS;
            }
        }
        DatacheckServerAppConfigPO model = DatacheckServerAppConfigMap.INSTANCES.dtoToPo(dto);
        if (StringUtils.isNotEmpty(dto.getAppPassword())) {
            byte[] base64Encrypt = EnCryptUtils.base64Encrypt(dto.getAppPassword());
            model.setAppPassword(new String(base64Encrypt));
        }
        int insert = baseMapper.insert(model);
        byte[] base64Encrypt = EnCryptUtils.base64Encrypt(model.appPassword);
        String pwd = new String(base64Encrypt);
        redisUtil.set(RedisKeyEnum.DATA_CHECK_SERVER_APP_ID +":"+ model.getAppAccount() + pwd,model.getId(), RedisKeyEnum.AUTH_USERINFO.getValue());
        if (insert > 0) {
            return ResultEnum.SUCCESS;
        } else {
            return ResultEnum.SAVE_DATA_ERROR;
        }
    }

    @Override
    public ResultEnum editData(AppRegisterEditDTO dto) {
        DatacheckServerAppConfigPO model = baseMapper.selectById(dto.getId());
        if (model == null) {
            return ResultEnum.DS_APP_EXISTS;
        }
        QueryWrapper<DatacheckServerAppConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().and(wq -> wq.eq(DatacheckServerAppConfigPO::getAppName, dto.getAppName()).
                        or().eq(DatacheckServerAppConfigPO::getAppAccount, dto.getAppAccount()))
                .eq(DatacheckServerAppConfigPO::getDelFlag, 1)
                .ne(DatacheckServerAppConfigPO::getId, dto.getId());
        List<DatacheckServerAppConfigPO> selectList = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(selectList)) {
            Optional<DatacheckServerAppConfigPO> appConfigOptional = selectList.stream().filter(item -> item.getAppName().equals(dto.getAppName())).findFirst();
            if (StringUtils.isNotEmpty(dto.getAppName()) && appConfigOptional.isPresent()) {
                return ResultEnum.DS_APP_NAME_EXISTS;
            }
            appConfigOptional = selectList.stream().filter(item -> item.getAppAccount().equals(dto.getAppAccount())).findFirst();
            if (StringUtils.isNotEmpty(dto.getAppAccount()) && appConfigOptional.isPresent()) {
                return ResultEnum.DS_APP_ACCOUNT_EXISTS;
            }
        }
        DatacheckServerAppConfigMap.INSTANCES.editDtoToPo(dto, model);
        int i = baseMapper.updateById(model);
        if (i > 0) {
            return ResultEnum.SUCCESS;
        } else {
            return ResultEnum.SAVE_DATA_ERROR;
        }
    }

    @Override
    public ResultEnum deleteData(int id) {
        DatacheckServerAppConfigPO model = baseMapper.selectById(id);
        if (model == null) {
            return ResultEnum.DS_APP_EXISTS;
        }
//        // 查询应用下是否存在api
//        QueryWrapper<AppServiceConfigPO> queryWrapper = new QueryWrapper<>();
//        queryWrapper.lambda().eq(AppServiceConfigPO::getAppId, id).eq(AppServiceConfigPO::getApiState, 1)
//                .eq(AppServiceConfigPO::getDelFlag, 1)
//                .eq(AppServiceConfigPO::getType, AppServiceTypeEnum.API.getValue());
//        List<AppServiceConfigPO> appApiPOS = appApiMapper.selectList(queryWrapper);
//        if (CollectionUtils.isEmpty(appApiPOS)) {
//
            // 该应用下没有启用的api，可以直接删除
        int i = baseMapper.deleteByIdWithFill(model);
        if (i > 0) {
            return ResultEnum.SUCCESS;
        } else {
            return ResultEnum.SAVE_DATA_ERROR;
        }
    }

    @Override
    public List<FilterFieldDTO> getColumn() {
        MetaDataConfigDTO dto = new MetaDataConfigDTO();
        dto.url = url;
        dto.userName = username;
        dto.password = password;
        dto.driver = driver;
        dto.tableName = "tb_datacheck_server_app_config";
        dto.filterSql = FilterSqlConstants.DATA_CHECK_APP_REGISTRATION_SQL;
        return getMetadata.getMetadataList(dto);
    }

    @Override
    public ResultEnum appSubscribe(AppApiSubDTO dto) {
        return null;
    }
}
