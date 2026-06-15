package cn.org.alan.exam.model.vo.home;

import lombok.Data;

/**
 * 薄弱知识点VO
 */
@Data
public class WeakPointVO {
    
    /**
     * 知识点名称
     */
    private String knowledgePointName;
    
    /**
     * 所属科目
     */
    private String subject;
    
    /**
     * 错误次数
     */
    private Integer wrongCount;
    
    /**
     * 掌握程度（百分比）
     */
    private Double masteryRate;
    
    /**
     * 建议
     */
    private String suggestion;
}