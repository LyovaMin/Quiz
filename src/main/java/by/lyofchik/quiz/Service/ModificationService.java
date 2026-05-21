package by.lyofchik.quiz.Service;

import by.lyofchik.quiz.Model.DTO.Request.QuizRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Model.Entity.Quiz;
import by.lyofchik.quiz.Model.Entity.QuizTopic;
import by.lyofchik.quiz.Model.Entity.QuizTopicId;
import by.lyofchik.quiz.Model.Entity.User;
import by.lyofchik.quiz.Model.Mapper.QuizMapper;
import by.lyofchik.quiz.Repository.QuizTopicRepository;
import by.lyofchik.quiz.Repository.QuizzesRepository;
import by.lyofchik.quiz.Repository.TopicRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class ModificationService {
    QuizzesRepository quizzesRepository;
    QuizTopicRepository quizTopicRepository;
    TopicRepository topicRepository;
    QuizMapper quizMapper;
    AuthService authService;

    public Response createQuiz(QuizRq request, User currentUser) {
        if (currentUser == null) {
            return Response.error("401", "Login required");
        }
        try {
            Response topicValidation = validateTopics(request);
            if (!topicValidation.getStatus().startsWith("2")) {
                return topicValidation;
            }
            request.setCreatedBy(currentUser.getId());
            Quiz newQuiz = quizMapper.toQuiz(request);
            log.info("QuizzesController.createQuiz - {}", newQuiz);
            quizzesRepository.save(newQuiz);
            saveTopics(newQuiz.getId(), request.getTopicIds());
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
        Response topicValidation = validateTopics(request);
        if (!topicValidation.getStatus().startsWith("2")) {
            return topicValidation;
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
        saveTopics(quiz.getId(), request.getTopicIds());
        return Response.success();
    }

    private Response validateTopics(QuizRq request) {
        if (request.getTopicIds() == null || request.getTopicIds().isEmpty()) {
            return Response.error("400", "Choose from 1 to 3 topics");
        }
        var distinctTopicIds = request.getTopicIds().stream().distinct().toList();
        if (distinctTopicIds.size() > 3) {
            return Response.error("400", "Choose no more than 3 topics");
        }
        if (topicRepository.findAllById(distinctTopicIds).size() != distinctTopicIds.size()) {
            return Response.error("400", "Unknown topic");
        }
        request.setTopicIds(distinctTopicIds);
        return Response.success();
    }

    private void saveTopics(Integer quizId, java.util.List<Integer> topicIds) {
        quizTopicRepository.deleteByQuiz(quizId);
        topicIds.forEach(topicId -> topicRepository.findById(topicId).ifPresent(topic -> {
            QuizTopic quizTopic = new QuizTopic();
            QuizTopicId id = new QuizTopicId();
            id.setQuizId(quizId);
            id.setTopicId(topicId);
            quizTopic.setId(id);
            quizTopic.setQuiz(quizId);
            quizTopic.setTopic(topic);
            quizTopicRepository.save(quizTopic);
        }));
    }

    private boolean canManage(Quiz quiz, User currentUser) {
        return currentUser != null && (authService.isAdmin(currentUser) || quiz.getCreator().equals(currentUser.getId()));
    }
}
