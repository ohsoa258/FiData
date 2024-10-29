package pd.tangqiao.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import pd.tangqiao.entity.TqDataQualityCheckPO;
import pd.tangqiao.service.TqDataQualityCheckPOService;

import javax.annotation.Resource;

@Api(tags = {SwaggerConfig.TQ_TAG_38})
@RestController
@RequestMapping("/tqDataQualityCheck")
public class TqDataQualityCheckController {

    @Resource
    private TqDataQualityCheckPOService service;

    /**
     * 数据质量核查新增
     *
     * @param po
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "数据流程调度新增")
    public ResultEntity<Object> add(@RequestBody TqDataQualityCheckPO po) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.add(po));
    }

    /**
     * 数据质量核查回显
     *
     * @return
     */
    @GetMapping("/getCheckList")
    @ApiOperation(value = "数据流程调度回显")
    public ResultEntity<Object> getFlowList(@RequestParam("currentPage") Integer currentPage, @RequestParam("size") Integer size) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getFlowList(currentPage,size));
    }
}
