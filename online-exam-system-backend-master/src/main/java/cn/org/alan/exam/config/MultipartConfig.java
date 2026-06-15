package cn.org.alan.exam.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;
import java.io.File;

@Configuration
public class MultipartConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(100));
        factory.setMaxRequestSize(DataSize.ofMegabytes(100));

        String tempDir = System.getProperty("user.dir") + File.separator + "temp" + File.separator + "upload";
        File dir = new File(tempDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        factory.setLocation(tempDir);

        return factory.createMultipartConfig();
    }
}
