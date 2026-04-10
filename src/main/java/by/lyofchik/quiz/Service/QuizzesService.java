package by.lyofchik.quiz.Service;

import by.lyofchik.quiz.Model.DTO.Request.QuizRq;
import by.lyofchik.quiz.Model.DTO.Request.QuizzesRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Model.Entity.Answer;
import by.lyofchik.quiz.Model.Entity.Question;
import by.lyofchik.quiz.Model.Entity.Quiz;
import by.lyofchik.quiz.Model.Mapper.QuizMapper;
import by.lyofchik.quiz.Repository.QuizzesRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class QuizzesService {
    QuizzesRepository quizzesRepository;
    QuizMapper quizMapper;

    public Response allQuizzes(QuizzesRq request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        return Response.success(quizzesRepository.findAll(pageable));
    }

    public Response getQuiz(int id) {
        return Response.success(quizzesRepository.getQuizById(id));
    }
}
