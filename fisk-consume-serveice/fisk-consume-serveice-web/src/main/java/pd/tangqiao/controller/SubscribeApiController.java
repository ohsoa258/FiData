package pd.tangqiao.controller;

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
import pd.tangqiao.entity.TqSubscribeApiConfigPO;
import pd.tangqiao.service.TqSubscribeApiConfigService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-10-29
 * @Description:
 */
@Api(tags = {SwaggerConfig.TQ_TAG_17})
@RestController
@RequestMapping("/subscribeApi")
public class SubscribeApiController {


    @Resource
    TqSubscribeApiConfigService service;

    @ApiOperation("添加api")
    @PostMapping("/getAll")
    public ResultEntity<List<TqSubscribeApiConfigPO>> getAll() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,service.getAll());
    }
    @ApiOperation("添加api")
    @PostMapping("/add")
    public ResultEntity<Object> addData(@RequestBody TqSubscribeApiConfigPO po) {
        return ResultEntityBuild.build(service.addData(po));
    }

    @ApiOperation("编辑api")
    @PostMapping("/edit")
    public ResultEntity<Object> editData(@RequestBody TqSubscribeApiConfigPO po) {
        return ResultEntityBuild.build(service.editData(po));
    }
}
