package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.dto.apiservice.TokenDTO;
import com.fisk.dataservice.dto.serviceanalyse.ATVServiceAnalyseDTO;
import com.fisk.dataservice.entity.*;
import com.fisk.dataservice.enums.AppServiceTypeEnum;
import com.fisk.dataservice.enums.LogLevelTypeEnum;
import com.fisk.dataservice.enums.LogTypeEnum;
import com.fisk.dataservice.mapper.*;
import com.fisk.dataservice.service.IATVServiceAnalyseService;
import com.fisk.dataservice.util.HttpUtils;
import com.fisk.dataservice.vo.atvserviceanalyse.AtvCallApiFuSingAnalyseVO;
import com.fisk.dataservice.vo.atvserviceanalyse.AtvYasCallApiAnalyseVO;
import com.fisk.dataservice.vo.atvserviceanalyse.AtvTopCallApiAnalyseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-22 11:34
 * @description
 */
@Service
@Slf4j
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

    @Value("${dataservice.scan.api_address}")
    private String scanApiAddress;

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

        long apiServiceCount = 0;

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
                        .filter(e -> e.getServiceId() == configPO.id)
                        .filter(e -> e.getServiceId() != 0).count();
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
                        .filter(e -> e.getServiceId() == tableServicePO.id)
                        .filter(e -> e.getServiceId() != 0).count();
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
                    .filter(v -> v.getViewThemeId() == viewThemePO.id)
                    .count();
        }

        analyseDTO.setServiceNumber(apiServiceCount);
        analyseDTO.setServiceCount(logPOS.size());
        return analyseDTO;
    }

    @Override
    public AtvCallApiFuSingAnalyseVO getAtvCallApiFuSingAnalyse() {
        return logsMapper.getAtvCallApiFuSingAnalyse();
    }

    @Override
    public List<AtvYasCallApiAnalyseVO> getAtvYasCallApiAnalyse() {
        return logsMapper.getAtvYasCallApiAnalyse();
    }

    @Override
    public List<AtvTopCallApiAnalyseVO> getAtvTopCallApiAnalyse() {
        return logsMapper.getAtvTopCallApiAnalyse();
    }

    @Async
    @Override
    public boolean scanDataServiceApiIsFuSing() {
        LogPO logPO = new LogPO();
        logPO.setLogLevel(LogLevelTypeEnum.DEBUG.getName());
        logPO.setLogType(LogTypeEnum.SCAN_API.getValue());
        try {
            String url = scanApiAddress + "/dataservice/apiService/getToken";
            TokenDTO tokenDTO = new TokenDTO();
            tokenDTO.setAppAccount("fiData_DataService_ScanTest_Account");
            tokenDTO.setAppPassword("fiData_DataService_ScanTest_Password");
            String getTokenParams = JSONObject.toJSONString(tokenDTO);
            ResultEntity result = HttpUtils.sendPostWebRequest(ResultEntity.class,
                    url, getTokenParams, null);
            if (result != null && result.getCode() == ResultEnum.DS_APISERVICE_API_APPINFO_EXISTS.getCode()) {
                logPO.setBusinessState("成功");
            } else {
                logPO.setBusinessState("失败");
            }
        } catch (Exception ex) {
            logPO.setBusinessState("失败");
            log.error("定时扫描数据服务API是否熔断，扫描异常：" + ex);
        } finally {
            try {
                logsMapper.insert(logPO);
            } catch (Exception se) {
                log.error("定时扫描数据服务API是否熔断，日志保存异常：" + se);
            }
        }
        return true;
    }
}
