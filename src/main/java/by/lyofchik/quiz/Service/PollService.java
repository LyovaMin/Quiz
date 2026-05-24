package by.lyofchik.quiz.Service;

import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Model.Entity.Answer;
import by.lyofchik.quiz.Model.Entity.PollVote;
import by.lyofchik.quiz.Model.Entity.Quiz;
import by.lyofchik.quiz.Model.Entity.User;
import by.lyofchik.quiz.Repository.AnswerRepository;
import by.lyofchik.quiz.Repository.PollVoteRepository;
import by.lyofchik.quiz.Repository.QuizzesRepository;
import by.lyofchik.quiz.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class PollService {

    private final PollVoteRepository pollVoteRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final QuizzesRepository quizzesRepository;

    @Transactional
    public Response vote(Integer quizId, Integer answerId, Integer userId) {
        if (pollVoteRepository.findByUserIdAndQuizId(userId, quizId).isPresent()) {
            return Response.error("400", "You have already voted in this poll.");
        }

        Optional<Answer> answerOpt = answerRepository.findById(answerId);
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Quiz> quizOpt = quizzesRepository.findById(quizId);

        if (answerOpt.isEmpty() || userOpt.isEmpty() || quizOpt.isEmpty()) {
            return Response.error("404", "Invalid data provided.");
        }

        Answer answer = answerOpt.get();
        answer.setVotes(answer.getVotes() + 1);
        answerRepository.save(answer);

        PollVote vote = new PollVote();
        vote.setUser(userOpt.get());
        vote.setQuiz(quizOpt.get());
        vote.setAnswer(answer);
        pollVoteRepository.save(vote);

        return Response.success(getPollResults(quizId));
    }

    public Response getPollResults(Integer quizId) {
        Quiz quiz = quizzesRepository.findById(quizId).orElse(null);
        if (quiz == null) {
            return Response.error("404", "Poll not found.");
        }
        return Response.success(quiz);
    }
}
