package com.fisk.task.listener.nifi.impl;

import com.alibaba.fastjson.JSON;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.client.DataAccessClient;
import com.fisk.dataaccess.dto.api.ApiImportDataDTO;
import com.fisk.task.listener.nifi.INonRealTimeListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author cfk
 */
@Component
@Slf4j
public class BuildNonRealTimeListener implements INonRealTimeListener {
    @Resource
    DataAccessClient dataAccessClient;

    @Override
    public ResultEnum importData(String dto, Acknowledgment acke) {
        try {
            //ApiImportDataDTO
            log.info("非实时api同步参数:{}",dto);
            ApiImportDataDTO apiImportDataDTO = JSON.parseObject(dto, ApiImportDataDTO.class);
            ResultEntity<Object> objectResultEntity = dataAccessClient.importData(apiImportDataDTO);
            if (Objects.equals(objectResultEntity.code, ResultEnum.SUCCESS.getCode())) {
                return ResultEnum.SUCCESS;
            }
            return ResultEnum.ERROR;
        } catch (Exception e) {
            log.error("执行非实时api同步报错");
            return ResultEnum.ERROR;
        } finally {
            if (acke != null) {
                acke.acknowledge();
            }
        }
    }
}
