package cn.org.alan.exam.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC配置类 - 配置静态资源映射
 * 
 * @author AI Assistant
 * @version 1.0
 * @since 2026/05/01
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * 配置静态资源映射
     * 将 /uploads/** 映射到本地文件系统的上传目录
     * 将 /static/** 映射到classpath下的static目录
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保上传目录存在
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        // 获取绝对路径
        String absolutePath = uploadDirectory.getAbsolutePath();
        
        // 配置上传文件的静态资源映射
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath + "/");
        
        // 配置默认头像的静态资源映射
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        
        System.out.println("配置静态资源映射：/uploads/** -> " + absolutePath + "/");
        System.out.println("配置静态资源映射：/static/** -> classpath:/static/");
    }
}