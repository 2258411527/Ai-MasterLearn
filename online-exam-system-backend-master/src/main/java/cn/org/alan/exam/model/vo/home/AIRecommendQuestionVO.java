package cn.org.alan.exam.model.vo.home;

import lombok.Data;
import java.util.List;

@Data
public class AIRecommendQuestionVO {
    private Integer id;
    private String content;
    private Integer quType;
    private String knowledgePoint;
    private String analysis;
    private List<AnswerOption> options;
    private String recommendReason;
    private String difficulty;
    private Integer quTypeId;
    private String answer;

    @Data
    public static class AnswerOption {
        private String key;
        private String content;
        private Boolean isCorrect;
    }
}
