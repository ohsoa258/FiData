package com.fisk.mdm.dto.masterdata;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author JianWenYang
 * date 2022/05/06 11:26
 */
@Data
public class ImportParamDTO {

    public int versionId;

    public int entityId;

    public int modelId;

    public boolean removeSpace;

    public int pageSize;

    public int pageIndex;

}
