package cn.org.alan.exam.service;

/**
 * AI聊天服务接口
 *
 * @author AI Assistant
 * @since 2024
 */
public interface IAiChatService {

    /**
     * AI对话（使用默认配置）
     *
     * @param question 用户问题
     * @return AI回答
     */
    String chat(String question);

    /**
     * AI对话（指定配置ID）
     *
     * @param question 用户问题
     * @param configId AI配置ID（可选，为null时使用默认配置）
     * @return AI回答
     */
    String chat(String question, Integer configId);
}