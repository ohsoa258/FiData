package pd.tangqiao.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import pd.tangqiao.config.TqSwaggerConfig;
import pd.tangqiao.entity.TqDatasourceConfigPO;

import javax.annotation.Resource;

@Api(tags = {TqSwaggerConfig.TAG_1})
@RestController
@RequestMapping("/tqDatasourceConfig")
public class TqDatasourceConfigController {

    @Resource
    private pd.tangqiao.service.TqDatasourceConfigPOService service;

    /**
     * 添加数据源
     *
     * @param po
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加数据源")
    public ResultEntity<Object> add(@RequestBody TqDatasourceConfigPO po) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.add(po));
    }

    /**
     * 编辑数据源
     *
     * @param po
     * @return
     */
    @PostMapping("/edit")
    @ApiOperation(value = "编辑数据源")
    public ResultEntity<Object> edit(@RequestBody TqDatasourceConfigPO po) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.edit(po));
    }

    /**
     * 删除数据源
     *
     * @param id
     * @return
     */
    @PostMapping("/del")
    @ApiOperation(value = "删除数据源")
    public ResultEntity<Object> del(@RequestParam("id") Integer id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.del(id));
    }

    /**
     * 分页回显
     *
     * @param currentPage
     * @param size
     * @return
     */
    @ApiOperation(value = "分页回显")
    @GetMapping("/pageFilter")
    public ResultEntity<Object> pageFilter(@RequestParam("currentPage") Integer currentPage, @RequestParam("size") Integer size) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.pageFilter(currentPage, size));
    }

}
