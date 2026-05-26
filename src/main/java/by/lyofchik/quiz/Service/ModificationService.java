package by.lyofchik.quiz.Service;

import by.lyofchik.quiz.Model.DTO.Request.QuizRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Model.Entity.Quiz;
import by.lyofchik.quiz.Model.Entity.QuizTopic;
import by.lyofchik.quiz.Model.Entity.QuizTopicId;
import by.lyofchik.quiz.Model.Entity.User;
import by.lyofchik.quiz.Model.Enum.Type;
import by.lyofchik.quiz.Model.Mapper.QuizMapper;
import by.lyofchik.quiz.Repository.QuizTopicRepository;
import by.lyofchik.quiz.Repository.QuizzesRepository;
import by.lyofchik.quiz.Repository.TopicRepository;
import by.lyofchik.quiz.Repository.LobbyRepository;
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
    LobbyRepository lobbyRepository;
    QuizMapper quizMapper;
    AuthService authService;

    public Response createQuiz(QuizRq request, User currentUser) {
        if (currentUser == null) {
            return Response.error("401", "Login required");
        }
        if (Type.valueOf(request.getType()) == Type.POLL && request.getQuestions().size() != 1) {
            return Response.error("400", "Poll can only have one question");
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
            saveTopics(newQuiz, request.getTopicIds());
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
        // delete dependent lobbies first to avoid FK constraint errors
        lobbyRepository.deleteByQuiz(quiz.get());
        quizzesRepository.deleteById(id);
        return Response.success();
    }

    public Response updateQuiz(int id, QuizRq request, User currentUser) {
        log.info("Updating quiz {} with data: {}", id, request);
        var optionalQuiz = quizzesRepository.findById(id);
        if (optionalQuiz.isEmpty()) {
            log.error("Quiz with id {} not found for update.", id);
            return Response.error("404", "Quiz not found");
        }
        var quiz = optionalQuiz.get();
        if (!canManage(quiz, currentUser)) {
            log.warn("User {} attempted to update quiz {} without permission.", currentUser.getId(), id);
            return Response.error("403", "You are not the creator of this quiz");
        }
        if (Type.valueOf(request.getType()) == Type.POLL && request.getQuestions().size() != 1) {
            return Response.error("400", "Poll can only have one question");
        }
        Response topicValidation = validateTopics(request);
        if (!topicValidation.getStatus().startsWith("2")) {
            log.warn("Topic validation failed for quiz {}: {}", id, topicValidation.getMessage());
            return topicValidation;
        }

        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setTimeLimit(request.getTimeLimitSeconds());
        quiz.setIsPublic(request.isPublic());
        quiz.setType(Type.valueOf(request.getType()));

        Quiz tempQuiz = quizMapper.toQuiz(request);
        quiz.getQuestions().clear();
        quiz.getQuestions().addAll(tempQuiz.getQuestions());
        
        quiz.getQuestions().forEach(question -> {
            question.setQuiz(quiz);
            question.getAnswers().forEach(answer -> answer.setQuestion(question));
        });

        Quiz updatedQuiz = quizzesRepository.save(quiz);
        saveTopics(updatedQuiz, request.getTopicIds());
        
        log.info("Successfully updated quiz {}", id);
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
        for (Integer topicId : distinctTopicIds) {
            if (topicId == null) {
                log.error("Received null topicId in the list: {}", request.getTopicIds());
                return Response.error("400", "Topic ID cannot be null.");
            }
            if (!topicRepository.existsById(topicId)) {
                log.error("Topic with id {} does not exist.", topicId);
                return Response.error("400", "Unknown topic with id: " + topicId);
            }
        }
        request.setTopicIds(distinctTopicIds);
        return Response.success();
    }

    private void saveTopics(Quiz quiz, java.util.List<Integer> topicIds) {
        log.info("Saving topics for quiz {}: {}", quiz.getId(), topicIds);
        quizTopicRepository.deleteByQuiz(quiz);
        topicIds.forEach(topicId -> {
            if (topicId == null) {
                log.error("Attempted to save a null topicId for quiz {}", quiz.getId());
                return;
            }
            topicRepository.findById(topicId).ifPresent(topic -> {
                QuizTopic quizTopic = new QuizTopic();
                QuizTopicId id = new QuizTopicId();
                id.setQuizId(quiz.getId());
                id.setTopicId(topicId);
                quizTopic.setId(id);
                quizTopic.setQuiz(quiz);
                quizTopic.setTopic(topic);
                quizTopicRepository.save(quizTopic);
            });
        });
    }

    private boolean canManage(Quiz quiz, User currentUser) {
        return currentUser != null && (authService.isAdmin(currentUser) || quiz.getCreator().getId().equals(currentUser.getId()));
    }
}