package com.fisk.dataservice.vo.fileservice;

import com.fisk.dataservice.dto.fileservice.FileServiceDTO;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class FileServiceVO extends FileServiceDTO {

    /**
     * 是否订阅 1：已订阅 0：未订阅
     */
    public Integer fileServiceSubState;
}
