package pd.tangqiao.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pd.tangqiao.entity.TqDatacheckReportQueryDTO;
import pd.tangqiao.entity.TqDatacheckReportPO;
import pd.tangqiao.entity.TqDatacheckReportVO;
import pd.tangqiao.service.TqDatacheckReportService;

import javax.annotation.Resource;

/**
 * @Author: wangjian
 * @Date: 2024-10-28
 * @Description:
 */
@Api(tags = {SwaggerConfig.TQ_TAG_13})
@RestController
@RequestMapping("/tqcheck")
public class DataCheckController {


    @Resource
    TqDatacheckReportService service;
    @ApiOperation("分页查询所有api")
    @PostMapping("/page")
    public ResultEntity<Page<TqDatacheckReportVO>> getAll(@RequestBody TqDatacheckReportQueryDTO po) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll(po));
    }
    @ApiOperation("添加")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@RequestBody TqDatacheckReportPO po) {
        return ResultEntityBuild.build(service.addData(po));
    }
}
