package pd.tangqiao.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import pd.tangqiao.entity.TqDataQualityCheckPO;
import pd.tangqiao.entity.TqLogStoragePO;
import pd.tangqiao.service.TqDataQualityCheckPOService;
import pd.tangqiao.service.TqLogStoragePOService;

import javax.annotation.Resource;

@Api(tags = {SwaggerConfig.TQ_TAG_39})
@RestController
@RequestMapping("/tqLogStorage")
public class TqLogStorageController {

    @Resource
    private TqLogStoragePOService service;

    /**
     * 日志存储新增
     *
     * @param po
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "日志存储新增")
    public ResultEntity<Object> add(@RequestBody TqLogStoragePO po) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.add(po));
    }

    /**
     * 日志存储回显
     *
     * @return
     */
    @GetMapping("/getLogList")
    @ApiOperation(value = "日志存储回显")
    public ResultEntity<Object> getLogList(@RequestParam("currentPage") Integer currentPage, @RequestParam("size") Integer size) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getLogList(currentPage,size));
    }
}
