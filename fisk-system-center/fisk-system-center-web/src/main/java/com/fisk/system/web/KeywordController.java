package com.fisk.system.web;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.system.config.SwaggerConfig;
import com.fisk.system.dto.KeywordTypeDTO;
import com.fisk.system.service.IKeywordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;


/**
 * @author lock
 */
@Api(tags = {SwaggerConfig.KEYWORD})
@RestController
@RequestMapping("/keywords")
public class KeywordController {

    @Resource
    private IKeywordService service;

    /**
     * 根据数据源类型查询SQL关键字集合
     *
     * @param dto 请求参数
     * @return 返回值
     */
    @PostMapping("/getListByType")
    @ApiOperation(value = "根据数据源类型查询SQL关键字集合")
    public ResultEntity<List<String>> getList(@RequestBody KeywordTypeDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.getList(dto));
    }

    @PostMapping("/judgeKeyWord")
    @ApiOperation(value = "根据输入的字符串判断是否为关键字")
    public ResultEntity<Object> judgeKeyWord(@Validated @RequestBody KeywordTypeDTO dto) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS, service.judgeKeyWord(dto));
    }
}
