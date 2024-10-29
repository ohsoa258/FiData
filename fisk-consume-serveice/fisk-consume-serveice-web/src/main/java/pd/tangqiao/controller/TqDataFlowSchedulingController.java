package pd.tangqiao.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import pd.tangqiao.entity.TqDataFlowSchedulingPO;
import pd.tangqiao.service.TqDataFlowSchedulingPOService;

import javax.annotation.Resource;

@Api(tags = {SwaggerConfig.TQ_TAG_37})
@RestController
@RequestMapping("/tqDataFlowScheduling")
public class TqDataFlowSchedulingController {

    @Resource
    private TqDataFlowSchedulingPOService service;

    /**
     * 数据流程调度新增
     *
     * @param po
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "数据流程调度新增")
    public ResultEntity<Object> add(@RequestBody TqDataFlowSchedulingPO po) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.add(po));
    }

    /**
     * 数据流程调度回显
     *
     * @return
     */
    @GetMapping("/getFlowList")
    @ApiOperation(value = "数据流程调度回显")
    public ResultEntity<Object> getFlowList() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFlowList());
    }

}
