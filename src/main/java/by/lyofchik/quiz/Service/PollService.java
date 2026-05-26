package by.lyofchik.quiz.Service;

import by.lyofchik.quiz.Model.DTO.Request.QuizRq;
import by.lyofchik.quiz.Model.DTO.Response.PollResultDTO;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Model.Entity.Answer;
import by.lyofchik.quiz.Model.Entity.PollVote;
import by.lyofchik.quiz.Model.Entity.Quiz;
import by.lyofchik.quiz.Model.Entity.User;
import by.lyofchik.quiz.Model.Enum.Type;
import by.lyofchik.quiz.Model.Mapper.QuizMapper;
import by.lyofchik.quiz.Repository.AnswerRepository;
import by.lyofchik.quiz.Repository.PollVoteRepository;
import by.lyofchik.quiz.Repository.QuizzesRepository;
import by.lyofchik.quiz.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PollService {

    private final PollVoteRepository pollVoteRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final QuizzesRepository quizzesRepository;
    private final QuizMapper quizMapper;

    @Transactional
    public Response createPoll(QuizRq request, User currentUser) {
        if (currentUser == null) {
            return Response.error("401", "Login required");
        }
        if (request.getQuestions() == null || request.getQuestions().size() != 1) {
            return Response.error("400", "Poll can have only one question");
        }
        Quiz quiz = quizMapper.toQuiz(request);
        quiz.setCreator(currentUser);
        quiz.setType(Type.POLL);
        quizzesRepository.save(quiz);
        return Response.success(quiz);
    }

    @Transactional
    public Response vote(Integer quizId, Integer answerId, User currentUser) {
        if (currentUser == null) {
            return Response.error("401", "Login required");
        }
        if (pollVoteRepository.findByUserIdAndQuizId(currentUser.getId(), quizId).isPresent()) {
            return Response.error("400", "You have already voted in this poll.");
        }

        Optional<Answer> answerOpt = answerRepository.findById(answerId);
        Optional<Quiz> quizOpt = quizzesRepository.findById(quizId);

        if (answerOpt.isEmpty() || quizOpt.isEmpty()) {
            return Response.error("404", "Invalid data provided.");
        }

        Answer answer = answerOpt.get();
        answer.setVotes(answer.getVotes() == null ? 1 : answer.getVotes() + 1);
        answerRepository.save(answer);

        PollVote vote = new PollVote();
        vote.setUser(currentUser);
        vote.setQuiz(quizOpt.get());
        vote.setAnswer(answer);
        pollVoteRepository.save(vote);

        return getPollResults(quizId, currentUser);
    }

    public Response getPollResults(Integer quizId, User currentUser) {
        Quiz quiz = quizzesRepository.findById(quizId).orElse(null);
        if (quiz == null) {
            return Response.error("404", "Poll not found.");
        }

        int totalVotes = quiz.getQuestions().stream()
                .flatMap(question -> question.getAnswers().stream())
                .mapToInt(answer -> answer.getVotes() == null ? 0 : answer.getVotes())
                .sum();

        List<PollResultDTO> results = quiz.getQuestions().stream()
                .flatMap(question -> question.getAnswers().stream())
                .map(answer -> {
                    int votes = answer.getVotes() == null ? 0 : answer.getVotes();
                    double percentage = totalVotes == 0 ? 0 : (double) votes / totalVotes * 100;
                    return new PollResultDTO(answer.getId(), answer.getText(), votes, percentage);
                })
                .collect(Collectors.toList());

        boolean userHasVoted = currentUser != null && pollVoteRepository.findByUserIdAndQuizId(currentUser.getId(), quizId).isPresent();

        return Response.success(Map.of(
                "results", results,
                "userHasVoted", userHasVoted
        ));
    }
}