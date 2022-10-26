package com.fisk.task.controller;

import com.fisk.common.core.response.ResultEntity;
import com.fisk.common.core.response.ResultEntityBuild;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.task.service.task.ITBETLIncremental;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 *
 * @author cfk
 */
@Slf4j
@RestController
@RequestMapping("/TBETLIncremental")
public class TBETLIncrementalController {

    @Resource
    ITBETLIncremental itbetlIncremental;

    /**
     * 拼接sql替换时间
     *
     * @param tableName tableName
     * @param sql sql
     * @param driveType driveType
     * @return 返回值
     */
    @GetMapping("/converSql")
    public ResultEntity<Map<String, String>> converSql(
            @RequestParam("tableName") String tableName,
            @RequestParam("sql") String sql,
            @RequestParam(value = "driveType", required = false) String driveType,
            @RequestParam(value = "deltaTimes", required = false) List<DeltaTimeDTO> deltaTimes) {
        return ResultEntityBuild.build(ResultEnum.SUCCESS,itbetlIncremental.converSql(tableName,sql, driveType,deltaTimes));
    }
}
