package by.lyofchik.quiz.Model.Mapper;

import by.lyofchik.quiz.Model.DTO.AnswerDTO;
import by.lyofchik.quiz.Model.DTO.QuestionDTO;
import by.lyofchik.quiz.Model.DTO.Request.QuizRq;
import by.lyofchik.quiz.Model.Entity.Answer;
import by.lyofchik.quiz.Model.Entity.Question;
import by.lyofchik.quiz.Model.Entity.Quiz;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface QuizMapper {
    @Mapping(source = "image", target = "imageUrl")
    @Mapping(source = "createdBy", target = "creator")
    @Mapping(source = "timeLimitSeconds", target = "timeLimit")
    @Mapping(target = "id", ignore = true)
    Quiz toQuiz(QuizRq request);

    @Mapping(source = "image", target = "imageUrl")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "quiz", ignore = true)
    Question toQuestion(QuestionDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "question", ignore = true)
    Answer toAnswer(AnswerDTO dto);


    @Mapping(source = "imageUrl", target = "image")
    @Mapping(source = "creator", target = "createdBy")
    @Mapping(source = "timeLimit", target = "timeLimitSeconds")
    QuizRq toQuizRq(Quiz quiz);

    @Mapping(source = "imageUrl", target = "image")
    QuestionDTO toQuestionDTO(Question question);

    AnswerDTO toAnswerDTO(Answer answer);

    @Mapping(source = "image", target = "imageUrl")
    @Mapping(source = "createdBy", target = "creator")
    @Mapping(source = "timeLimitSeconds", target = "timeLimit")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questions", ignore = true)
    void updateQuizFromRq(QuizRq request, @MappingTarget Quiz quiz);

    @AfterMapping
    default void linkRelations(@MappingTarget Quiz quiz) {
        quiz.getQuestions().forEach(question -> {
            question.setQuiz(quiz);
            question.getAnswers().forEach(answer -> answer.setQuestion(question));
        });
    }
}