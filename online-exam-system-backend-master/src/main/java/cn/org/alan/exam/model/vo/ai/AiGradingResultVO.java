package cn.org.alan.exam.model.vo.ai;

import lombok.Data;

@Data
public class AiGradingResultVO {
    private Integer questionId;
    private Integer questionType;
    private String questionContent;
    private String userAnswer;
    private String standardAnswer;
    private Integer aiScore;
    private Boolean isCorrect;
    private String detailedAnalysis;
    private String improvementSuggestions;
    private String knowledgePoints;
    private String optionAnalysis;
    private String similarQuestions;
    private String scoringCriteria;
    private String learningPath;
}