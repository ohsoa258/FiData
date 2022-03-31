package com.fisk.chartvisual.controller;

import com.fisk.chartvisual.dto.ChinaMapDTO;
import com.fisk.chartvisual.service.ChinaMapService;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author WangYan
 * @date 2021/10/28 17:16
 */
@RestController
@RequestMapping("/map")
public class ChinaMapController {

    @Resource
    ChinaMapService mapService;

    @GetMapping("/getAll")
    @ApiOperation("获取中国省份信息")
    public ResultEntity<List<ChinaMapDTO>> getAll() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, mapService.getAll());
    }
}
