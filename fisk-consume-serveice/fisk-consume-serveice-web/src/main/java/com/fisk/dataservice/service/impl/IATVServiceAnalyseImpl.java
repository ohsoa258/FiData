package com.fisk.dataservice.service.impl;

import com.fisk.dataservice.dto.serviceanalyse.ATVServiceAnalyseDTO;
import com.fisk.dataservice.entity.*;
import com.fisk.dataservice.enums.AppServiceTypeEnum;
import com.fisk.dataservice.mapper.*;
import com.fisk.dataservice.service.IATVServiceAnalyseService;
import com.fisk.dataservice.service.ITableService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-22 11:34
 * @description
 */
@Service
public class IATVServiceAnalyseImpl implements IATVServiceAnalyseService {

    @Resource
    private AppRegisterMapper appRegisterMapper; //api 应用

    @Resource
    private TableAppMapper tableAppMapper; //数据库同步 table 应用

    @Resource
    private ViewThemeMapper viewThemeMapper; //视图 应用

    @Resource
    private ApiRegisterMapper apiRegisterMapper; //api应用下的服务

    @Resource
    private TableServiceMapper tableServiceMapper; //table 应用下的表

    @Resource
    private ViewMapper viewMapper; //视图应用下的视图

    @Resource
    private LogsMapper logsMapper; //日志

    @Resource
    private AppServiceConfigMapper serviceConfigMapper; // 服务应用中间表



    @Override
    public ATVServiceAnalyseDTO getServiceAnalyse() {
        /**
         * analyseDTO 创建封装数据分析数据
         * serviceConfigPOS 应用ID对应的服务
         * apiServiceCount 服务总数记录
         * logPOS 日志记录服务调用的次数
         */
        List<LogPO> logPOS = logsMapper.selectList(null);
        ATVServiceAnalyseDTO analyseDTO = new ATVServiceAnalyseDTO();

        List<AppServiceConfigPO> serviceConfigPOS = serviceConfigMapper.selectList(null);

        long apiServiceCount=0;

        /**
         * api服务
         *
         * appConfigPOS 应用
         *
         * apiConfigPOS 服务
         */
        List<AppConfigPO> appConfigPOS = appRegisterMapper.selectList(null);

        List<ApiConfigPO> apiConfigPOS = apiRegisterMapper.selectList(null);

        //API服务 type类型为1
        for (AppConfigPO appConfigPO : appConfigPOS) {
            for (ApiConfigPO configPO : apiConfigPOS) {
                apiServiceCount += serviceConfigPOS.stream()
                        .filter(e -> e.getAppId() == appConfigPO.getId())
                        .filter(e -> e.getType() == AppServiceTypeEnum.API.getValue())
                        .filter(e->e.getServiceId()==configPO.id)
                        .filter(e->e.getServiceId()!=0).count();
            }
        }

        /**
         * 数据库同步的表服务
         * tableAppPOS 应用
         * tableServicePOS 表服务
         */
        List<TableAppPO> tableAppPOS = tableAppMapper.selectList(null);

        List<TableServicePO> tableServicePOS = tableServiceMapper.selectList(null);
        //表服务 type类型为2
        for (TableAppPO tableAppPO : tableAppPOS) {
            for (TableServicePO tableServicePO : tableServicePOS) {
                apiServiceCount += serviceConfigPOS.stream()
                        .filter(e -> e.getAppId() == tableAppPO.getId())
                        .filter(e -> e.getType() == AppServiceTypeEnum.TABLE.getValue())
                        .filter(e->e.getServiceId()==tableServicePO.id)
                        .filter(e->e.getServiceId()!=0).count();
            }
        }

        /**
         * 视图服务
         * viewThemePOS 应用
         * viewPOS 服务
         */
        List<ViewThemePO> viewThemePOS = viewThemeMapper.selectList(null);

        List<ViewPO> viewPOS = viewMapper.selectList(null);
        for (ViewThemePO viewThemePO : viewThemePOS) {
            apiServiceCount += viewPOS.stream()
                    .filter(v->v.getViewThemeId()==viewThemePO.id)
                    .count();
        }

        analyseDTO.setServiceNumber(apiServiceCount);
        analyseDTO.setServiceCount(logPOS.size());
        return analyseDTO;
    }

}
