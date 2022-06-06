package com.fisk.mdm.dto.masterdata;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author JianWenYang
 * date 2022/05/06 11:26
 */
@Data
public class ImportParamDTO extends MasterDataBaseDTO {

    public boolean removeSpace;

}
