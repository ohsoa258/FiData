package com.fisk.datamanage.client;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.datamanagement.dto.dataquality.DataQualityDTO;
import com.fisk.datamanagement.dto.dataquality.UpperLowerBloodParameterDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author JianWenYang
 */
@FeignClient("datamanagement-service")
public interface DataManageClient {

    /**
     * 数据源是否存在atlas
     * @param dto
     * @return
     */
    @PostMapping("/DataQuality/existAtlas")
    ResultEntity<Object> existAtlas(@Validated @RequestBody DataQualityDTO dto);

    /**
     * 是否存在上下血缘
     * @param dto
     * @return
     */
    @PostMapping("/DataQuality/existUpperLowerBlood")
    ResultEntity<Object> existUpperLowerBlood(@Validated @RequestBody UpperLowerBloodParameterDTO dto);

}
