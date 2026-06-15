package cn.org.alan.exam.converter;

import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.form.question.QuestionFrom;
import cn.org.alan.exam.model.vo.exercise.QuestionSheetVO;
import cn.org.alan.exam.model.vo.question.QuestionVO;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * QuestionConverter 的手动实现类（使用 @Primary 确保优先使用）
 */
@Component
@Primary
public class QuestionConverterManualImpl implements QuestionConverter {

    @Override
    public Question fromToEntity(QuestionFrom questionFrom) {
        if (questionFrom == null) {
            return null;
        }
        Question question = new Question();
        question.setRepoId(questionFrom.getRepoId());
        question.setContent(questionFrom.getContent());
        question.setQuType(questionFrom.getQuType());
        question.setAnalysis(questionFrom.getAnalysis());
        return question;
    }

    @Override
    public List<QuestionSheetVO> listEntityToVO(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return new ArrayList<>();
        }
        List<QuestionSheetVO> result = new ArrayList<>();
        for (Question question : questions) {
            result.add(entityToVO(question));
        }
        return result;
    }

    @Override
    public QuestionSheetVO entityToVO(Question question) {
        if (question == null) {
            return null;
        }
        QuestionSheetVO vo = new QuestionSheetVO();
        vo.setQuId(question.getId());
        vo.setQuType(question.getQuType());
        vo.setRepoId(question.getRepoId());
        return vo;
    }

    @Override
    public QuestionVO QuestionToQuestionVO(Question question) {
        if (question == null) {
            return null;
        }
        QuestionVO vo = new QuestionVO();
        vo.setId(question.getId());
        vo.setContent(question.getContent());
        vo.setQuType(question.getQuType());
        vo.setAnalysis(question.getAnalysis());
        vo.setRepoId(question.getRepoId());
        return vo;
    }
}