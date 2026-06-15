package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.service.IFileService;
import cn.org.alan.exam.utils.file.FileService;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Objects;

/**
 * 说明：
 *
 * @Author Alan
 * @Version 1.0
 * @Date 2025/3/21 10:44 PM
 */
@Service
@Slf4j
public class FileServiceImpl implements IFileService {
    @Resource
    private FileService fileService;

    @SneakyThrows(IOException.class)
    @Override
    public Result<String> uploadImage(MultipartFile file) {
        log.info("开始上传图片，文件名：{}，文件大小：{}字节", file.getOriginalFilename(), file.getSize());
        
        if (!fileService.isImage(Objects.requireNonNull(file.getOriginalFilename()))) {
            log.warn("上传的文件不是常用图片格式：{}", file.getOriginalFilename());
            throw new ServiceRuntimeException("上传头像到文件不是常用图片格式(png、jpg、jpeg、bmp)");
        }
        
        if (fileService.isOverSize(file)) {
            log.warn("图片大小超过限制：{}字节（最大2MB）", file.getSize());
            throw new ServiceRuntimeException("图片大小不能超过2MB");
        }
        
        String url = fileService.upload(file);
        log.info("图片上传成功，返回URL：{}", url);
        
        if (StringUtils.isBlank(url)) {
            log.error("图片上传失败，url地址为空");
            throw new ServiceRuntimeException("图片上传失败，url地址没有返回");
        }
        
        return Result.success("图片上传成功", url);
    }

}
