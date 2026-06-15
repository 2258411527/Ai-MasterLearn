package cn.org.alan.exam.model.form.study;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

/**
 * 学习资料上传表单
 *
 * @author AI Assistant
 * @since 2024
 */
@Data
public class StudyMaterialForm {
    
    /**
     * 上传的文件
     */
    @NotNull(message = "请选择要上传的文件")
    private MultipartFile file;
    
    /**
     * 文件描述（可选）
     */
    private String description;
}