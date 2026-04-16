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
            log.info("QuizzesController.createQuiz - {}", newQuiz);
            quizzesRepository.save(newQuiz);
            return Response.success();
        }  catch (Exception e) {
            log.error(e.getMessage());
            return Response.error();
        }
    }

    public Response deleteQuiz(int id) {
        quizzesRepository.deleteById(id);
        return Response.success();
    }

    public Response updateQuiz(int id, QuizRq request) {
        Quiz quiz = quizMapper.toQuiz(request);
        if(quiz.getCreator().equals(id)) {
            quizMapper.updateQuizFromRq(request, quiz);
            return Response.success();
        }
        return Response.error();
    }
}
