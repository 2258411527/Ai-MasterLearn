package cn.org.alan.exam.model.vo.home;

import lombok.Data;

import java.util.List;

/**
 * 错题分析VO
 */
@Data
public class WrongAnalysisVO {
    
    /**
     * 总错题数
     */
    private Integer totalWrongCount;
    
    /**
     * 薄弱知识点列表
     */
    private List<WeakPointVO> weakPoints;
    
    /**
     * AI分析建议
     */
    private String aiSuggestion;
}