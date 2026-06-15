package cn.org.alan.exam.model.vo.discussion;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author WeiJin
 * @version 1.0
 * @since 2025/4/3 9:43
 */
@Data
public class PageDiscussionVo {
    private Integer id;
    private String title;
    private String sender;
    private String avatar;
    private String role;
    private Integer commentCount;
    private Integer likeCount;
    private Integer viewCount;
    private Integer visibility;
    private Boolean liked;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
