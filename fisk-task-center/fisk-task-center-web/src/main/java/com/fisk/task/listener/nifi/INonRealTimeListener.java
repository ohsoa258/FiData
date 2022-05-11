package com.fisk.task.listener.nifi;

import com.fisk.common.core.response.ResultEnum;
import com.fisk.dataaccess.dto.api.ApiImportDataDTO;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.constraints.NotNull;
/**
 * @author cfk
 */
public interface INonRealTimeListener {
    ResultEnum importData( String dto, Acknowledgment acke);
}
