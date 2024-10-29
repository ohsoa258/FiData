package pd.tangqiao.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pd.tangqiao.entity.TqApiConfigPO;
import pd.tangqiao.entity.TqApiConfigQueryDTO;
import pd.tangqiao.entity.TqApiConfigVO;
import pd.tangqiao.service.TqApiConfigService;

import javax.annotation.Resource;

/**
 * @Author: wangjian
 * @Date: 2024-10-29
 * @Description:
 */
@Api(tags = {SwaggerConfig.TQ_TAG_15})
@RestController
@RequestMapping("/api")
public class ApiController {
    @Resource
    TqApiConfigService service;

//    @ApiOperation(value = "查询所有应用")
//    @GetMapping("/getAll")
//    public ResultEntity<Page<TqApiConfigVO>> getAll(@RequestBody Page<TqApiConfigVO> page) {
//        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll(page));
//    }

    @ApiOperation("分页查询所有api")
    @PostMapping("/page")
    public ResultEntity<Page<TqApiConfigVO>> getAll(@RequestBody TqApiConfigQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll(dto));
    }
    @ApiOperation("添加api")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@RequestBody TqApiConfigPO po) {
        return ResultEntityBuild.build(service.addData(po));
    }

    @ApiOperation("编辑api")
    @PostMapping("/edit")
    public ResultEntity<Object> editData(@RequestBody TqApiConfigPO po) {
        return ResultEntityBuild.build(service.editData(po));
    }
}
