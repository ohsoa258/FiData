package pd.tangqiao.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import pd.tangqiao.entity.TqLabelDataManagementPO;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = {SwaggerConfig.TQ_TAG_35})
@RestController
@RequestMapping("/tqLabelDataManagement")
public class TqLabelDataManagementController {

    @Resource
    private pd.tangqiao.service.TqLabelDataManagementPOService service;

    /**
     * 为指定规则添加标签
     *
     * @param dtos
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "为指定规则添加标签")
    public ResultEntity<Object> add(@RequestBody List<TqLabelDataManagementPO> dtos) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.add(dtos));
    }

    /**
     * 获取标签集合
     *
     * @return
     */
    @GetMapping("/getLables")
    @ApiOperation(value = "获取标签集合")
    public ResultEntity<Object> getLables() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getLables());
    }

}
