package com.fisk.common.service.mdmBEOperate.impl;

import com.fisk.common.service.mdmBEOperate.IBuildCodeCommand;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author JianWenYang
 */
public class BuildRandomCode implements IBuildCodeCommand {

    @Override
    public String createCode() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String getCurrentTime() {
        return String.valueOf(System.currentTimeMillis());
    }

    @Override
    public String fixedValue(String value) {
        return value;
    }

    @Override
    public String getFiledValue(String field, List<Map<String, String>> result) {
        AtomicReference<String> value = null;
        result.stream()
                .forEach(e -> {
                    for (String key : e.keySet()) {
                        if (key.equals(field)){
                            value.set(e.get(key));
                        }
                    }
                });
        return value.get();
    }
}
