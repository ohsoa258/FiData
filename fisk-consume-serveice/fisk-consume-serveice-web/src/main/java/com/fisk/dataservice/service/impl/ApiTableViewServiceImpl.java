package com.fisk.dataservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fisk.common.core.enums.dataservice.DataSourceTypeEnum;
import com.fisk.common.server.metadata.AppBusinessInfoDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataDbAttributeDTO;
import com.fisk.common.service.metadata.dto.metadata.MetaDataInstanceAttributeDTO;
import com.fisk.dataservice.entity.AppConfigPO;
import com.fisk.dataservice.entity.DataSourceConPO;
import com.fisk.dataservice.entity.TableAppPO;
import com.fisk.dataservice.entity.ViewThemePO;
import com.fisk.dataservice.mapper.*;
import com.fisk.dataservice.service.IApiTableViewService;
import com.fisk.dataservice.vo.app.AppApiSubVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-04-03 14:50
 * @description
 */
@Service
public class ApiTableViewServiceImpl implements IApiTableViewService {
    @Resource
    private AppRegisterMapper appRegisterMapper;

    @Resource
    private TableAppMapper tableAppMapper;

    @Resource
    private ViewThemeMapper viewThemeMapper;

    @Resource
    private DataSourceConMapper dataSourceConMapper;

    @Resource
    private AppServiceConfigMapper serviceConfigMapper;

    @Override
    public List<AppBusinessInfoDTO> getApiTableViewService() {
        //封装三个服务的所有应用
        List<AppBusinessInfoDTO> appInfos=new ArrayList<>();
        List<AppConfigPO> apiAppConfigPOS = appRegisterMapper.selectList(null);
        List<TableAppPO> tableAppPOS = tableAppMapper.selectList(null);
        List<ViewThemePO> viewThemeAppPOS = viewThemeMapper.selectList(null);
        //封装API服务的所有应用
        apiAppConfigPOS.stream()
                .forEach(a -> {
                    AppBusinessInfoDTO infoDTO = new AppBusinessInfoDTO(a.getId(),a.getAppName(),a.getAppPrincipal(),a.getAppDesc(),3);
                    appInfos.add(infoDTO);
                });
        //封装Table服务的所有应用
        tableAppPOS.stream()
                .forEach(a -> {
                    AppBusinessInfoDTO infoDTO = new AppBusinessInfoDTO(a.getId(),a.getAppName(),a.getAppPrincipal(),a.getAppDesc(),4);
                    appInfos.add(infoDTO);
                });
        //封装View服务的所有应用
        viewThemeAppPOS.stream()
                .forEach(a -> {
                    AppBusinessInfoDTO infoDTO = new AppBusinessInfoDTO(a.getId(),a.getThemeName(),a.getThemeAbbr(),a.getThemeDesc(),5);
                    appInfos.add(infoDTO);
                });
        return appInfos;
    }

    @Override
    public List<MetaDataInstanceAttributeDTO> synchronizationAPIAppRegistration() {
        List<AppConfigPO> apiAppConfigPOS = appRegisterMapper.selectList(null);
        if (CollectionUtils.isEmpty(apiAppConfigPOS)) {
            return new ArrayList<>();
        }
        List<MetaDataInstanceAttributeDTO> list = new ArrayList<>();
        QueryWrapper<DataSourceConPO> wrapper=null;
        for (AppConfigPO appConfigPO : apiAppConfigPOS) {
            //根据应用找到对应下的api服务
            List<AppApiSubVO> apiSubVOS = serviceConfigMapper.getAppByIdApiService(appConfigPO.id);
            wrapper = new QueryWrapper<>();
            wrapper.in("id",apiSubVOS.stream().map(a->a.dataSourceId).collect(Collectors.toList()));
            //根据API服务的数据源ID找到对应的数据源
            List<DataSourceConPO> dataSourceConPOS = dataSourceConMapper.selectList(wrapper);
            for (DataSourceConPO dataSourceConPO : dataSourceConPOS) {
                if(dataSourceConPO==null){
                    continue;
                }

                List<MetaDataInstanceAttributeDTO> data =addDataSourceMetaData(appConfigPO, dataSourceConPO);
                if (CollectionUtils.isEmpty(data)) {
                    continue;
                }

                list.addAll(data);
            }
        }

        return list;
    }

    /**
     * 新增API服务元数据信息
     * @param appConfigPO
     * @param dataSource
     * @return
     */
    public List<MetaDataInstanceAttributeDTO> addDataSourceMetaData(AppConfigPO appConfigPO, DataSourceConPO dataSource) {
        if (DataSourceTypeEnum.getEnum(dataSource.conType).getName().equals("API")
                || DataSourceTypeEnum.getEnum(dataSource.conType).getName().equals("RESTFULAPI")
                || DataSourceTypeEnum.getEnum(dataSource.conType).getName().equals("SFTP")
                || DataSourceTypeEnum.getEnum(dataSource.conType).getName().equals("FTP")) {
            return null;
        }
        List<MetaDataInstanceAttributeDTO> list = new ArrayList<>();
        MetaDataInstanceAttributeDTO data = new MetaDataInstanceAttributeDTO();
        data.name = dataSource.conIp + "_" + appConfigPO.getAppPrincipal();
        data.hostname = dataSource.conIp;
        data.port = String.valueOf(dataSource.conPort);
        data.qualifiedName = appConfigPO.id + "_" + appConfigPO.getAppPrincipal();
        data.rdbms_type = DataSourceTypeEnum.getEnum(dataSource.conType).getName().toLowerCase();
        data.displayName = appConfigPO.getAppName();
        data.description = "stg";
        data.comment = String.valueOf(dataSource.id);
        //库
        List<MetaDataDbAttributeDTO> dbList = new ArrayList<>();
        MetaDataDbAttributeDTO db = new MetaDataDbAttributeDTO();
        db.name = dataSource.name;
        db.displayName = dataSource.name;
        db.qualifiedName = data.qualifiedName + "_" + dataSource.id;
        dbList.add(db);
        data.dbList = dbList;

        list.add(data);

        return list;

    }
}
