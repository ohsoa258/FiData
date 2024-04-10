package com.fisk.datamanagement.dto.metadataentity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author JinXingWang
 */
@Data
public class UpdateMetadataExpiresTimeDto {

    public Integer entityId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss")
    public LocalDateTime expiresTime;
}
