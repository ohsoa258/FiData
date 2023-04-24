package com.fisk.task.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.service.accessAndTask.DataTranDTO;
import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.task.config.SwaggerConfig;
import com.fisk.task.service.task.ITBETLIncremental;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 *
 * @author cfk
 */
@Api(tags = {SwaggerConfig.TBETLIncremental})
@Slf4j
@RestController
@RequestMapping("/TBETLIncremental")
public class TBETLIncrementalController {

    @Resource
    ITBETLIncremental itbetlIncremental;

    /**
     * 拼接sql替换时间
     *
     * @return 返回值
     */
    @PostMapping("/converSql")
    public ResultEntity<Map<String, String>> converSql(@RequestBody DataTranDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,itbetlIncremental.converSql(dto.tableName, dto.querySql, dto.driveType, dto.deltaTimes));
    }
}
