package by.lyofchik.quiz.Model.Mapper;

import by.lyofchik.quiz.Model.DTO.Request.GameRq;
import by.lyofchik.quiz.Model.Entity.QuestionStat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnalyticsMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "answer", ignore = true)
    @Mapping(source = "request.attemptId", target = "attempt")
    @Mapping(source = "request.questionId", target = "question")
    @Mapping(source = "request.answer", target = "answerText")
    @Mapping(source = "isCorrect", target = "isCorrect")
    @Mapping(source = "request.startedAt", target = "startedAt")
    @Mapping(source = "request.completedAt", target = "completedAt")
    QuestionStat toQuestionStat(GameRq request, boolean isCorrect);
}
