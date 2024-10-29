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
import pd.tangqiao.entity.TqCenterApiConfigPO;
import pd.tangqiao.entity.TqCenterApiConfigQueryDTO;
import pd.tangqiao.entity.TqCenterApiConfigVO;
import pd.tangqiao.service.TqCenterApiConfigService;

import javax.annotation.Resource;

/**
 * @Author: wangjian
 * @Date: 2024-10-29
 * @Description:
 */
@Api(tags = {SwaggerConfig.TQ_TAG_19})
@RestController
@RequestMapping("/centerApi")
public class CenterApiController {
    @Resource
    TqCenterApiConfigService service;

    @ApiOperation("分页查询所有api")
    @PostMapping("/page")
    public ResultEntity<Page<TqCenterApiConfigVO>> getAll(@RequestBody TqCenterApiConfigQueryDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getAll(dto));
    }
    @ApiOperation("添加api")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@RequestBody TqCenterApiConfigPO po) {
        return ResultEntityBuild.build(service.addData(po));
    }

    @ApiOperation("编辑api")
    @PostMapping("/edit")
    public ResultEntity<Object> editData(@RequestBody TqCenterApiConfigPO po) {
        return ResultEntityBuild.build(service.editData(po));
    }
}
