package com.fisk.common.service.mdmBEOperate.impl;

import com.fisk.common.service.mdmBEOperate.IBuildCodeCommand;

import java.util.UUID;

/**
 * @author JianWenYang
 */
public class BuildRandomCode implements IBuildCodeCommand {

    @Override
    public String createCode() {
        return UUID.randomUUID().toString();
    }

}
