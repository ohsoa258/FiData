package com.fisk.mdm.dto.access;
import com.fisk.task.dto.accessmdm.AccessAttributeDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author jianwenyang
 */
@Data
public class AccessAttributeAddDTO{
    public int accessId;
    /**
     * 是否发布
     */
    public boolean isPublish;
    public int createType;
    /**
     * 覆盖脚本
     */
    public String coverScript;
    /**
     * 发布备注
     */
    public String remark;
    public List<AccessAttributeDTO> list;

    public SyncModeDTO syncModeDTO;
    /**
     * 是否同步
     */
    public boolean openTransmission;

    /**
     * 接入的增量时间参数
     */
    public List<DeltaTimeDTO> deltaTimes;
    /**
     * 自定义脚本集合
     */
    public List<CustomScriptDTO> customScriptList;

    public Long userId;
    public LocalDateTime sendTime;
    public Long logId;
    public String traceId;
    public String spanId;
}
