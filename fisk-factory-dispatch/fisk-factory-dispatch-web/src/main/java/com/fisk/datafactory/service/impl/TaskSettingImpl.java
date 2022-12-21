package com.fisk.datafactory.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fisk.common.core.response.ResultEnum;
import com.fisk.common.framework.exception.FkException;
import com.fisk.dataaccess.dto.sftp.SftpUploadDTO;
import com.fisk.datafactory.dto.customworkflowdetail.TaskSettingDTO;
import com.fisk.datafactory.entity.TaskSettingPO;
import com.fisk.datafactory.map.TaskSettingMap;
import com.fisk.datafactory.mapper.TaskSettingMapper;
import com.fisk.datafactory.service.ITaskSetting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author cfk
 */
@Slf4j
@Service
public class TaskSettingImpl extends ServiceImpl<TaskSettingMapper, TaskSettingPO> implements ITaskSetting {

    @Resource
    TaskSettingMapper taskSettingMapper;


    @Override
    public ResultEnum deleteByTaskId(long taskId) {
        taskSettingMapper.deleteByTaskId(taskId);
        return ResultEnum.SUCCESS;
    }

    @Override
    public ResultEnum updateTaskSetting(long taskId, Map<String, String> taskSetting) {
        //map转list
        List<TaskSettingPO> list = new ArrayList<>();
        Iterator<Map.Entry<String, String>> nodeMap = taskSetting.entrySet().iterator();
        while (nodeMap.hasNext()) {
            Map.Entry<String, String> nodeEntry = nodeMap.next();
            String key = nodeEntry.getKey();
            String value = nodeEntry.getValue();
            TaskSettingPO taskSettingPo = new TaskSettingPO();
            taskSettingPo.settingKey = key;
            taskSettingPo.value = value;
            taskSettingPo.taskId = String.valueOf(taskId);
            list.add(JSON.parseObject(JSON.toJSONString(taskSettingPo), TaskSettingPO.class));
        }
        taskSettingMapper.deleteByTaskId(taskId);
        this.saveBatch(list);
        return ResultEnum.SUCCESS;
    }

    @Override
    public List<TaskSettingDTO> getTaskSettingsByTaskId(long taskId) {
        QueryWrapper<TaskSettingPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        return TaskSettingMap.INSTANCES.poToDto(taskSettingMapper.selectList(queryWrapper));
    }

    @Override
    public SftpUploadDTO uploadSecretKeyFile(Integer pipelineId, Integer taskId, MultipartFile file, int sourceOrTarget) {
        try {
            String filePath = "/root/upload/";
            String fileName = file.getOriginalFilename();
            String path = filePath + pipelineId + "/" + taskId + "/" + sourceOrTarget + "/";
            //如果不存在,创建文件夹
            File f = new File(path);
            if (!f.exists()) {
                f.mkdirs();
            }

            //指定到上传路径
            filePath = path + "/" + fileName;

            //创建新文件对象 指定文件路径为拼接好的路径
            File newFile = new File(filePath);
            //将前端传递过来的文件输送给新文件 这里需要抛出IO异常 throws IOException
            file.transferTo(newFile);

            SftpUploadDTO data = new SftpUploadDTO();
            data.uploadPath = filePath;
            return data;
        } catch (IOException e) {
            log.error("sftp上传秘钥文件失败,{}", e);
            throw new FkException(ResultEnum.UPLOAD_ERROR);
        }
    }


}
