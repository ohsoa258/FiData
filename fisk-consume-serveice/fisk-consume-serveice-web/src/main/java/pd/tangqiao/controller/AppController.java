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
import pd.tangqiao.entity.TqAppConfigPO;
import pd.tangqiao.entity.TqAppConfigVO;
import pd.tangqiao.service.TqAppConfigService;

import javax.annotation.Resource;

/**
 * @Author: wangjian
 * @Date: 2024-10-29
 * @Description:
 */
@Api(tags = {SwaggerConfig.TQ_TAG_14})
@RestController
@RequestMapping("/app")
public class AppController {


    @Resource
    TqAppConfigService service;

    @ApiOperation(value = "查询所有应用")
    @PostMapping("/getAll")
    public ResultEntity<Page<TqAppConfigVO>> getAll(@RequestBody Page<TqAppConfigVO> page) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll(page));
    }
    @ApiOperation("添加应用")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@RequestBody TqAppConfigPO po) {
        return ResultEntityBuild.build(service.addData(po));
    }

    @ApiOperation("编辑应用")
    @PostMapping("/edit")
    public ResultEntity<Object> editData(@RequestBody TqAppConfigPO po) {
        return ResultEntityBuild.build(service.editData(po));
    }

    @ApiOperation("删除应用")
    @DeleteMapping("/delete/{appId}")
    public ResultEntity<Object> deleteData(@PathVariable("appId") int appId) {
        return ResultEntityBuild.build(service.deleteData(appId));
    }
}
