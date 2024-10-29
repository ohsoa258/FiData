package pd.tangqiao.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataservice.config.SwaggerConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import pd.tangqiao.entity.TqDataEncryptionConfigPO;
import pd.tangqiao.service.TqDataEncryptionConfigPOService;

import javax.annotation.Resource;

@Api(tags = {SwaggerConfig.TQ_TAG_43})
@RestController
@RequestMapping("/tqDataEncryptionConfig")
public class TqDataEncryptionConfigController {

    @Resource
    private TqDataEncryptionConfigPOService service;

    /**
     * 数据脱敏加密管理新增
     *
     * @param po
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "数据脱敏加密管理新增")
    public ResultEntity<Object> add(@RequestBody TqDataEncryptionConfigPO po) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.add(po));
    }

    /**
     * 根据id修改指定数据的加密状态
     *
     * @param id
     * @return
     */
    @PostMapping("/editEncrypt")
    @ApiOperation(value = "根据id修改指定数据的加密状态")
    public ResultEntity<Object> editEncrypt(@RequestParam("id") Integer id) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.editEncrypt(id));
    }

    /**
     * 数据脱敏加密管理回显
     *
     * @return
     */
    @GetMapping("/getEncryptList")
    @ApiOperation(value = "数据脱敏加密管理回显")
    public ResultEntity<Object> getEncryptList(@RequestParam("currentPage") Integer currentPage, @RequestParam("size") Integer size) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getEncryptList(currentPage, size));
    }
}
