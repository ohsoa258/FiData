package pd.tangqiao.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import pd.tangqiao.entity.TqLogCollectionPO;
import pd.tangqiao.service.TqLogCollectionPOService;

import javax.annotation.Resource;

@Api(tags = {SwaggerConfig.TQ_TAG_40})
@RestController
@RequestMapping("/tqLogCollectionCon")
public class TqLogCollectionController {

    @Resource
    private TqLogCollectionPOService service;

    /**
     * 日志收集新增
     *
     * @param po
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "日志收集新增")
    public ResultEntity<Object> add(@RequestBody TqLogCollectionPO po) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.add(po));
    }

    /**
     * 日志收集回显
     *
     * @return
     */
    @GetMapping("/getCollectionList")
    @ApiOperation(value = "日志收集回显")
    public ResultEntity<Object> getCollectionList(@RequestParam("currentPage") Integer currentPage, @RequestParam("size") Integer size) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getCollectionList(currentPage, size));
    }
}
