package by.lyofchik.quiz.Model.Mapper;

import by.lyofchik.quiz.Model.DTO.AnswerDTO;
import by.lyofchik.quiz.Model.DTO.QuestionDTO;
import by.lyofchik.quiz.Model.DTO.Request.QuizRq;
import by.lyofchik.quiz.Model.Entity.Answer;
import by.lyofchik.quiz.Model.Entity.Question;
import by.lyofchik.quiz.Model.Entity.Quiz;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuizMapper {
    @Mapping(source = "question", target = "question")
    @Mapping(source = "dto.text", target = "text")
    @Mapping(source = "dto.isCorrect", target = "isCorrect")
    Answer toAnswer(AnswerDTO dto, int question);

    @Mapping(source = "quiz", target = "quiz")
    @Mapping(source = "dto.description", target = "description")
    @Mapping(source = "dto.type", target = "type")
    @Mapping(source = "dto.image", target = "imageUrl")
    @Mapping(source = "dto.points", target = "points")
    @Mapping(target = "answers", ignore = true)
    Question toQuestion(QuestionDTO dto, int quiz);

    @Mapping(source = "request.image", target = "imageUrl")
    @Mapping(source = "request.createdBy", target = "creator")
    @Mapping(source = "request.timeLimitSeconds", target = "timeLimit")
    @Mapping(target = "questions", ignore = true)
    Quiz toQuiz(QuizRq request);
}
