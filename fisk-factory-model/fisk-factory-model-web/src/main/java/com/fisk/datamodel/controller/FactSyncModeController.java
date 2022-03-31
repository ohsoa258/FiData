package com.fisk.datamodel.controller;

import com.fisk.datamodel.config.SwaggerConfig;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author JianWenYang
 */
@Api(tags = {SwaggerConfig.FACT_SYNC_MODE})
@RestController
@RequestMapping("/FactSyncMode")
@Slf4j
public class FactSyncModeController {
}
