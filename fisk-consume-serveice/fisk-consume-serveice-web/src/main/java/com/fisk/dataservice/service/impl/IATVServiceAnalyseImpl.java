package com.fisk.dataservice.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.core.utils.DateTimeUtils;
import com.fisk.dataservice.dto.apiservice.TokenDTO;
import com.fisk.dataservice.dto.atvserviceanalyse.AtvServiceMonitoringQueryDTO;
import com.fisk.dataservice.dto.serviceanalyse.ATVServiceAnalyseDTO;
import com.fisk.dataservice.entity.*;
import com.fisk.dataservice.enums.AppServiceTypeEnum;
import com.fisk.dataservice.mapper.*;
import com.fisk.dataservice.service.IATVServiceAnalyseService;
import com.fisk.dataservice.util.HttpUtils;
import com.fisk.dataservice.vo.app.AppApiBindVO;
import com.fisk.dataservice.vo.atvserviceanalyse.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
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
    private AppRegisterMapper appRegisterMapper; //app 应用

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

    @Resource
    private RedisTemplate redisTemplate;

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
        AtvCallApiFuSingAnalyseVO atvCallApiFuSingAnalyseVO = new AtvCallApiFuSingAnalyseVO();
        String redisKey = "fiData_DataService_ApiScanResult_Key";
        boolean flag = redisTemplate.hasKey(redisKey);
        if (!flag) {
            scanDataServiceApiIsFuSing();
        }
        String json = redisTemplate.opsForValue().get(redisKey).toString();
        if (StringUtils.isNotEmpty(json)) {
            atvCallApiFuSingAnalyseVO = JSONObject.parseObject(json, AtvCallApiFuSingAnalyseVO.class);
        }
        return atvCallApiFuSingAnalyseVO;
    }

    @Override
    public List<AtvYasCallApiAnalyseVO> getAtvYasCallApiAnalyse() {
        return logsMapper.getAtvYasCallApiAnalyse();
    }

    @Override
    public List<AtvTopCallApiAnalyseVO> getAtvTopCallApiAnalyse() {
        return logsMapper.getAtvTopCallApiAnalyse();
    }

    @Override
    public void scanDataServiceApiIsFuSing() {
        AtvCallApiFuSingAnalyseVO atvCallApiFuSingAnalyseVO = new AtvCallApiFuSingAnalyseVO();
        atvCallApiFuSingAnalyseVO.setLastScanDateTime(DateTimeUtils.getNow());
        try {
            String url = scanApiAddress + "/dataservice/apiService/getToken";
            log.info("【scanDataServiceApiIsFuSing-url】:" + url);
            TokenDTO tokenDTO = new TokenDTO();
            tokenDTO.setAppAccount("fiData_DataService_ScanTest_Account");
            tokenDTO.setAppPassword("fiData_DataService_ScanTest_Password");
            String getTokenParams = JSONObject.toJSONString(tokenDTO);
            ResultEntity result = HttpUtils.sendPostWebRequest(ResultEntity.class,
                    url, getTokenParams, null);
            if (result != null && result.getCode() == ResultEnum.DS_APISERVICE_API_APPINFO_EXISTS.getCode()) {
                atvCallApiFuSingAnalyseVO.setLastScanResult("成功");
            } else {
                atvCallApiFuSingAnalyseVO.setLastScanResult("失败");
            }
        } catch (Exception ex) {
            atvCallApiFuSingAnalyseVO.setLastScanResult("失败");
            log.error("定时扫描数据服务API是否熔断，扫描异常：" + ex);
        } finally {
            try {
                String json = JSONObject.toJSON(atvCallApiFuSingAnalyseVO).toString();
                String redisKey = "fiData_DataService_ApiScanResult_Key";
                redisTemplate.opsForValue().set(redisKey, json);
            } catch (Exception se) {
                log.error("定时扫描数据服务API是否熔断，redis写入异常：" + se);
            }
        }
    }

    @Override
    public AtvServiceDropdownCardVO getAtvServiceDropdownCard(AtvServiceMonitoringQueryDTO dto) {
        AtvServiceDropdownCardVO atvServiceDropdownCardVO = new AtvServiceDropdownCardVO();

        HashMap<Integer, String> createApiTypeList = new HashMap<>();
        createApiTypeList.put(0, "ALL");
        createApiTypeList.put(1, "本地API");
        createApiTypeList.put(3, "代理API");
        atvServiceDropdownCardVO.setCreateApiTypeList(createApiTypeList);

        HashMap<Integer, String> appNameList = new HashMap<>();
        appNameList.put(0, "ALL");
        HashMap<Integer, String> apiNameList = new HashMap<>();
        apiNameList.put(0, "ALL");

        List<AppApiBindVO> appApiBindList = serviceConfigMapper.getAppApiBindList(dto.getCreateApiType(), dto.getAppId(), dto.getApiId());
        if (CollectionUtils.isNotEmpty(appApiBindList)) {
            appApiBindList.forEach(t -> {
                if (!appNameList.containsKey(t.getAppId())) {
                    appNameList.put(t.getAppId(), t.getAppName());
                }
                if (!apiNameList.containsKey(t.getApiId())) {
                    apiNameList.put(t.getApiId(), t.getApiName());
                }
            });
        }

        atvServiceDropdownCardVO.setAppNameList(appNameList);
        atvServiceDropdownCardVO.setApiNameList(apiNameList);

        return atvServiceDropdownCardVO;
    }

    @Override
    public List<AtvApiTimeConsumingRankingVO> getAtvApiTimeConsumingRanking(AtvServiceMonitoringQueryDTO dto) {
        return logsMapper.getAtvApiTimeConsumingRanking(dto.getCreateApiType(),
                dto.getAppId(), dto.getApiId());
    }

    @Override
    public List<AtvApiSuccessFailureRankingVO> getAtvApiSuccessFailureRanking(AtvServiceMonitoringQueryDTO dto) {
        return logsMapper.getAtvApiSuccessFailureRanking(dto.getCreateApiType(),
                dto.getAppId(), dto.getApiId());
    }

    @Override
    public List<AtvApiPrincipalDetailAppBindApiVO> getAtvApiPrincipalDetailAppBindApi(AtvServiceMonitoringQueryDTO dto) {
        return logsMapper.getAtvApiPrincipalDetailAppBindApi(dto.getCreateApiType(),
                dto.getAppId(), dto.getApiId());
    }

    @Override
    public List<AtvApiSqCountApiBindAppRankingVO> getAtvApiSqCountApiBindAppRanking(AtvServiceMonitoringQueryDTO dto) {
        return logsMapper.getAtvApiSqCountApiBindAppRanking(dto.getCreateApiType(),
                dto.getAppId(), dto.getApiId());
    }
}
