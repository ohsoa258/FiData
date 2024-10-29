package pd.tangqiao.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import pd.tangqiao.entity.TqDataQualityTaskManagementPO;
import pd.tangqiao.service.TqDataQualityTaskManagementPOService;

import javax.annotation.Resource;

@Api(tags = {SwaggerConfig.TQ_TAG_34})
@RestController
@RequestMapping("/tqDataQualityTaskManagement")
public class TqDataQualityTaskManagementController {

    @Resource
    private TqDataQualityTaskManagementPOService service;

    /**
     * 添加质量任务
     *
     * @param po
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加质量任务")
    public ResultEntity<Object> add(@RequestBody TqDataQualityTaskManagementPO po) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.add(po));
    }

    /**
     * 查询质量任务集
     *
     * @return
     */
    @GetMapping("/getQualityTasks")
    @ApiOperation(value = "查询质量任务集")
    public ResultEntity<Object> getQualityTasks() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getQualityTasks());
    }

}
