package com.fisk.common.service.flinkupload;

import com.fisk.common.core.enums.flink.UploadWayEnum;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.common.service.flinkupload.impl.FlinkDevUpload;
import com.fisk.common.service.flinkupload.impl.FlinkSSHUpload;

/**
 * @author JianWenYang
 */
public class FlinkFactoryHelper {

    public static IFlinkJobUpload flinkUpload(UploadWayEnum wayEnum) {
        switch (wayEnum) {
            case DEV:
                return new FlinkDevUpload();
            case SSH:
                return new FlinkSSHUpload();
            default:
                throw new FkException(ResultEnum.ENUM_TYPE_ERROR);
        }
    }

}
