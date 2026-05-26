package by.lyofchik.quiz.Service;

import by.lyofchik.quiz.Model.DTO.Request.QuizRq;
import by.lyofchik.quiz.Model.DTO.Request.QuizzesRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Model.Entity.Quiz;
import by.lyofchik.quiz.Model.Entity.User;
import by.lyofchik.quiz.Model.Enum.Type;
import by.lyofchik.quiz.Model.Mapper.QuizMapper;
import by.lyofchik.quiz.Repository.QuizTopicRepository;
import by.lyofchik.quiz.Repository.QuizzesRepository;
import by.lyofchik.quiz.Repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class QuizzesService {
    private final QuizzesRepository quizzesRepository;
    private final QuizTopicRepository quizTopicRepository;
    private final UserRepository userRepository;
    private final QuizMapper quizMapper;

    public Response allQuizzes(QuizzesRq request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Type type = request.getType() != null ? Type.valueOf(request.getType()) : null;

        return Response.success(quizzesRepository
                .search(request.getSearch(), request.getTopicId(), type, pageable)
                .map(this::toQuizRqWithTopics));
    }

    public Response getQuiz(int id) {
        return Response.success(toQuizRqWithTopics(quizzesRepository.getQuizById(id)));
    }

    @Transactional
    public Response createQuiz(QuizRq request, User currentUser) {
        Quiz quiz = quizMapper.toQuiz(request);
        quiz.setCreator(currentUser);
        quizzesRepository.save(quiz);
        return Response.success(quizMapper.toQuizRq(quiz));
    }

    @Transactional
    public Response updateQuiz(int id, QuizRq request, User currentUser) {
        Quiz quiz = quizzesRepository.findById(id).orElse(null);
        if (quiz == null) {
            return Response.error("404", "Quiz not found");
        }
        if (quiz.getCreator().getId() != currentUser.getId() && !currentUser.getRole().equals("ADMIN")) {
            return Response.error("403", "You are not allowed to edit this quiz");
        }
        quizMapper.updateQuizFromRq(request, quiz);
        quizzesRepository.save(quiz);
        return Response.success(quizMapper.toQuizRq(quiz));
    }

    @Transactional
    public Response deleteQuiz(int id, User currentUser) {
        Quiz quiz = quizzesRepository.findById(id).orElse(null);
        if (quiz == null) {
            return Response.error("404", "Quiz not found");
        }
        if (quiz.getCreator().getId() != currentUser.getId() && !currentUser.getRole().equals("ADMIN")) {
            return Response.error("403", "You are not allowed to delete this quiz");
        }
        quizzesRepository.delete(quiz);
        return Response.success();
    }

    private QuizRq toQuizRqWithTopics(Quiz quiz) {
        if (quiz == null) {
            return null;
        }
        QuizRq dto = quizMapper.toQuizRq(quiz);
        var quizTopics = quizTopicRepository.findByQuiz(quiz);
        dto.setTopicIds(quizTopics.stream()
                .map(topic -> topic.getTopic().getId())
                .toList());
        dto.setTopics(quizTopics.stream()
                .map(topic -> topic.getTopic().getName().name())
                .toList());
        return dto;
    }
}