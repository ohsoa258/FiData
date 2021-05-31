package com.fisk.chartvisual.controller;

import com.fisk.chartvisual.dto.ChartQueryObject;
import com.fisk.chartvisual.service.IUseDataBase;
import com.fisk.chartvisual.vo.DataServiceVO;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
    public ResultEntity<List<DataServiceVO>> getData(@Validated @RequestBody ChartQueryObject query) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, db.query(query));
    }
}
