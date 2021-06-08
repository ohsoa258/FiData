package com.fisk.chartvisual.controller;

import com.fisk.chartvisual.dto.ChartQueryObject;
import com.fisk.chartvisual.service.IUseDataBase;
import com.fisk.chartvisual.vo.DataServiceVO;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 数据服务
 *
 * @author gy
 */
@RestController
@RequestMapping("/data")
@Slf4j
public class DataServiceController {

    @Resource
    IUseDataBase db;

    @PostMapping("/get")
    public ResultEntity<List<Map<String, Object>>> getMap(@Validated @RequestBody ChartQueryObject query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, db.query(query));
    }
}
