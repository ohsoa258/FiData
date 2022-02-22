package com.fisk.common.pdf.util;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author dick
 * @version 1.0
 * @description TDDD
 * @date 2022/2/7 17:22
 */
@Component
@Data
public class ConfigUtil {
    /**
     * 模板所在路径
     */
    @Value("${dataservice.pdf.path}")
    public String pdf_templatePath;
}

