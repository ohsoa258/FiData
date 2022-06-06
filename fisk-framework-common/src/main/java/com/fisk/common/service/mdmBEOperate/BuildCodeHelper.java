package com.fisk.common.service.mdmBEOperate;

import com.fisk.common.service.mdmBEOperate.impl.BuildRandomCode;

/**
 * @author JianWenYang
 */
public class BuildCodeHelper {

    public static IBuildCodeCommand getCodeCommand() {
        return new BuildRandomCode();
    }

}
