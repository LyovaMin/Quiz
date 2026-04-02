package by.lyofchik.quiz.Service;

import by.lyofchik.quiz.Model.DTO.Request.QuizRq;
import by.lyofchik.quiz.Model.DTO.Request.QuizzesRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Repository.QuizzesRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class QuizzesService {
    QuizzesRepository quizzesRepository;

    public Response allQuizzes(QuizzesRq request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        return Response.success(quizzesRepository.findAll(pageable));
    }

    public Response getQuiz(int id) {
        return Response.success(quizzesRepository.getQuizById(id));
    }


    public Response createQuiz(QuizRq request) {

    }

    public Response deleteQuiz(int id) {
        quizzesRepository.deleteById(id);
        return Response.success();
    }

    public Response updateQuiz(int id, QuizRq request) {
    }
}
