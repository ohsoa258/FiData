package com.fisk.datagovernance.client;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckSyncDTO;
import com.fisk.datagovernance.dto.dataquality.datacheck.DataCheckWebDTO;
import com.fisk.datagovernance.vo.dataquality.datacheck.DataCheckResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 数据质量服务接口
 * @date 2022/4/12 11:31
 */
@FeignClient("datagovernance-service")
public interface DataQualityClient {
    /**
     * 数据校验 界面/接口验证
     *
     * @param dto 请求参数
     * @return 执行结果
     */
    @PostMapping("/datacheck/interfaceCheckData")
    ResultEntity<List<DataCheckResultVO>> interfaceCheckData(@Validated @RequestBody DataCheckWebDTO dto);

    /**
     * 数据校验 同步验证
     *
     * @param dto 请求参数
     * @return AtlasEntityDbTableColumnDTO
     */
    @PostMapping("/datacheck/syncCheckData")
    ResultEntity<List<DataCheckResultVO>> syncCheckData(@Validated @RequestBody DataCheckSyncDTO dto);
}
