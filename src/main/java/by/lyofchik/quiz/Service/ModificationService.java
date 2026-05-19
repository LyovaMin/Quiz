package by.lyofchik.quiz.Service;

import by.lyofchik.quiz.Model.DTO.Request.QuizRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Model.Entity.Quiz;
import by.lyofchik.quiz.Model.Entity.User;
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
    AuthService authService;

    public Response createQuiz(QuizRq request, User currentUser) {
        if (currentUser == null) {
            return Response.error("401", "Login required");
        }
        try {
            request.setCreatedBy(currentUser.getId());
            Quiz newQuiz = quizMapper.toQuiz(request);
            log.info("QuizzesController.createQuiz - {}", newQuiz);
            quizzesRepository.save(newQuiz);
            return Response.success();
        }  catch (Exception e) {
            log.error(e.getMessage());
            return Response.error();
        }
    }

    public Response deleteQuiz(int id, User currentUser) {
        var quiz = quizzesRepository.findById(id);
        if (quiz.isEmpty()) {
            return Response.error("404", "Quiz not found");
        }
        if (!canManage(quiz.get(), currentUser)) {
            return Response.error("403", "You are not allowed to delete this quiz");
        }
        quizzesRepository.deleteById(id);
        return Response.success();
    }

    public Response updateQuiz(int id, QuizRq request, User currentUser) {
        var optionalQuiz = quizzesRepository.findById(id);
        if (optionalQuiz.isEmpty()) {
            return Response.error("404", "Quiz not found");
        }
        var quiz = optionalQuiz.get();
        if (!canManage(quiz, currentUser)) {
            return Response.error("403", "You are not the creator of this quiz");
        }
        request.setCreatedBy(quiz.getCreator());
        quizMapper.updateQuizFromRq(request, quiz);
        quiz.getQuestions().clear();
        Quiz incoming = quizMapper.toQuiz(request);
        incoming.getQuestions().forEach(question -> {
            question.setQuiz(quiz);
            question.getAnswers().forEach(answer -> answer.setQuestion(question));
            quiz.getQuestions().add(question);
        });
        quizzesRepository.save(quiz);
        return Response.success();
    }

    private boolean canManage(Quiz quiz, User currentUser) {
        return currentUser != null && (authService.isAdmin(currentUser) || quiz.getCreator().equals(currentUser.getId()));
    }
}
