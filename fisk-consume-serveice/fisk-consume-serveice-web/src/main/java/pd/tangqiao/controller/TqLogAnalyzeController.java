package pd.tangqiao.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import pd.tangqiao.entity.TqLogAnalyzePO;
import pd.tangqiao.service.TqLogAnalyzePOService;

import javax.annotation.Resource;

@Api(tags = {SwaggerConfig.TQ_TAG_41})
@RestController
@RequestMapping("/tqLogAnalyze")
public class TqLogAnalyzeController {

    @Resource
    private TqLogAnalyzePOService service;

    /**
     * 日志分析新增
     *
     * @param po
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "日志分析新增")
    public ResultEntity<Object> add(@RequestBody TqLogAnalyzePO po) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.add(po));
    }

    /**
     * 日志分析回显
     *
     * @return
     */
    @GetMapping("/getAnalyzeList")
    @ApiOperation(value = "日志分析回显")
    public ResultEntity<Object> getAnalyzeList(@RequestParam("currentPage") Integer currentPage, @RequestParam("size") Integer size) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAnalyzeList(currentPage, size));
    }
}
