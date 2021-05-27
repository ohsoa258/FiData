package com.fisk.chartvisual.controller;

import com.fisk.chartvisual.vo.DataServiceVO;
import com.fisk.common.response.ResultEntity;
import com.fisk.common.response.ResultEntityBuild;
import com.fisk.common.response.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/get")
    public ResultEntity<List<DataServiceVO>> getData() {
        List<DataServiceVO> data = new ArrayList<DataServiceVO>(){{
            add(new DataServiceVO(){{
                name = "智能发现";
                value = 100;
            }});
            add(new DataServiceVO(){{
                name = "主动上报";
                value = 75;
            }});
            add(new DataServiceVO(){{
                name = "巡查发现";
                value = 30;
            }});
        }};
        return ResultEntityBuild.build(ResultEnum.SUCCESS, data);
    }
}
