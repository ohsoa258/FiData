package com.fisk.datagovernance.service.impl.dataquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.datagovernance.entity.dataquality.AttachmentInfoPO;
import com.fisk.datagovernance.mapper.dataquality.AttachmentInfoMapper;
import com.fisk.datagovernance.service.dataquality.IAttachmentInfoManageService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

/**
 * @author dick
 * @version 1.0
 * @description 附件接口实现类
 * @date 2022/8/15 9:56
 */
@Service
public class AttachmentInfoImpl extends ServiceImpl<AttachmentInfoMapper, AttachmentInfoPO> implements IAttachmentInfoManageService {

    @Override
    public void downloadExcelFile(String objectId, String category, HttpServletResponse response) {
        try {
            if (StringUtils.isEmpty(objectId)) {
                return;
            }
            QueryWrapper<AttachmentInfoPO> attachmentInfoPOQueryWrapper = new QueryWrapper<>();
            attachmentInfoPOQueryWrapper.lambda().eq(AttachmentInfoPO::getDelFlag, 1)
                    .eq(AttachmentInfoPO::getObjectId, objectId)
                    .eq(AttachmentInfoPO::getCategory, category);
            AttachmentInfoPO attachmentInfoPO = baseMapper.selectOne(attachmentInfoPOQueryWrapper);
            if (attachmentInfoPO == null) {
                return;
            }
            String filePath = "";
            if (attachmentInfoPO.getAbsolutePath().endsWith("/")) {
                filePath = attachmentInfoPO.getAbsolutePath() + attachmentInfoPO.getCurrentFileName();
            } else {
                filePath = attachmentInfoPO.getAbsolutePath() + File.separator + attachmentInfoPO.getCurrentFileName();
            }
            File file = new File(filePath);
            // 取得文件名
            String filename = attachmentInfoPO.getOriginalName();
            // 以流的形式下载文件
            InputStream fis = new BufferedInputStream(new FileInputStream(filePath));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            // 清空response
            response.reset();
            // 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
            response.addHeader("Content-Length", "" + file.length());
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
        } catch (Exception ex) {
            log.error("【downloadExcelFile】 系统异常：" + ex);
            throw new FkException(ResultEnum.ERROR, "【downloadExcelFile】 ex：" + ex);
        }
        return;
    }
}
