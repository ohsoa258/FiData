package com.fisk.common.service.mdmBEOperate;

import com.fisk.common.service.mdmBEOperate.impl.BuildRandomCode;

import java.util.List;

/**
 * @author JianWenYang
 */
public class BuildCodeHelper {

    public static IBuildCodeCommand getCodeCommand() {
        return new BuildRandomCode();
    }

}
