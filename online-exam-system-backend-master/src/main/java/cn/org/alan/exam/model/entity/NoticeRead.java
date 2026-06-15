package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_notice_read")
public class NoticeRead implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("notice_id")
    private Integer noticeId;

    @TableField("user_id")
    private Integer userId;

    @TableField("read_time")
    private LocalDateTime readTime;
}
