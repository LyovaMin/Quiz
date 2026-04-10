package by.lyofchik.quiz.Service;

import by.lyofchik.quiz.Model.DTO.Request.QuizRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Model.Entity.Question;
import by.lyofchik.quiz.Model.Entity.Quiz;
import by.lyofchik.quiz.Model.Mapper.QuizMapper;
import by.lyofchik.quiz.Repository.QuizzesRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ModificationService {
    QuizzesRepository quizzesRepository;
    QuizMapper quizMapper;

    public Response createQuiz(QuizRq request) {
        try {
            Quiz newQuiz = quizMapper.toQuiz(request);
            request.getQuestions()
                    .forEach(q -> {
                        Question question = quizMapper.toQuestion(q, newQuiz.getId());
                        newQuiz.getQuestions().add(question);
                        q.getAnswers()
                                .forEach(a -> {
                                    question.getAnswers().add(quizMapper.toAnswer(a, newQuiz.getId()));
                                });
                    });
            quizzesRepository.save(newQuiz);
        }  catch (Exception e) {
            log.error(e.getMessage());
            Response.error();
        }
        return Response.success();
    }

    public Response deleteQuiz(int id) {
        quizzesRepository.deleteById(id);
        return Response.success();
    }

    public Response updateQuiz(int id, QuizRq request) {
        Quiz quiz = quizzesRepository.getQuizById(id);

    }
}
