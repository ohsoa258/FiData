package com.fisk.dataaccess.client;

import com.fisk.common.response.ResultEntity;
import com.fisk.task.dto.atlas.AtlasEntityDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Lock
 */
@FeignClient("dataAccess-service")
public interface DataAccessClient {

    /**
     *
     * 给task模块提供数据源等信息
     *
     * @param id appid
     * @return 执行结果
     */
    @GetMapping("/appRegistration/dataAccess")
    Long dataAccessConfig(@RequestParam("appid") long id);

    /**
     * 调用
     *
     * @param id
     * @return
     */
    @GetMapping("/appRegistration/getAtlasEntity/{id}")
    ResultEntity<AtlasEntityDTO> getAtlasEntity(@PathVariable("id") long id);

}
