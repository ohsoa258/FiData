package pd.tangqiao.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import pd.tangqiao.entity.TqViewsPermissionsConfigPO;
import pd.tangqiao.service.TqViewsPermissionsConfigPOService;

import javax.annotation.Resource;

@Api(tags = {SwaggerConfig.TQ_TAG_42})
@RestController
@RequestMapping("/tqViewsPermissions")
public class TqViewsPermissionsConfigController {

    @Resource
    private TqViewsPermissionsConfigPOService service;

    /**
     * 数据访问视图和权限管理新增
     *
     * @param po
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "数据访问视图和权限管理新增")
    public ResultEntity<Object> add(@RequestBody TqViewsPermissionsConfigPO po) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.add(po));
    }

    /**
     * 修改指定权限的开关状态
     *
     * @param id
     * @return
     */
    @PostMapping("/changePermissionById")
    @ApiOperation(value = "修改指定权限的开关状态")
    public ResultEntity<Object> changePermissionById(@RequestParam("id") Integer id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.changePermissionById(id));
    }

    /**
     * 数据访问视图和权限管理回显
     *
     * @return
     */
    @GetMapping("/getVPList")
    @ApiOperation(value = "数据访问视图和权限管理回显")
    public ResultEntity<Object> getVPList(@RequestParam("currentPage") Integer currentPage, @RequestParam("size") Integer size) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getVPList(currentPage, size));
    }
}
