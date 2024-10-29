package pd.tangqiao.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import pd.tangqiao.entity.TqQuestionBankPO;
import pd.tangqiao.service.TqQuestionBankPOService;

import javax.annotation.Resource;

@Api(tags = {SwaggerConfig.TQ_TAG_36})
@RestController
@RequestMapping("/tqQuestionBank")
public class TqQuestionBankController {

    @Resource
    private TqQuestionBankPOService service;

    /**
     * 问题库新增
     *
     * @param po
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "问题库新增")
    public ResultEntity<Object> add(@RequestBody TqQuestionBankPO po) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.add(po));
    }

    /**
     * 问题库回显
     *
     * @return
     */
    @GetMapping("/getBanks")
    @ApiOperation(value = "问题库回显")
    public ResultEntity<Object> getBanks() {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getBanks());
    }

}
