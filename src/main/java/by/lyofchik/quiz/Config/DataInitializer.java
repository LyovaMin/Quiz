package by.lyofchik.quiz.Config;

import by.lyofchik.quiz.Model.Entity.Topic;
import by.lyofchik.quiz.Model.Enum.TopicName;
import by.lyofchik.quiz.Repository.TopicRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {
    private final TopicRepository topicRepository;

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        if (topicRepository.count() == 0) {
            for (TopicName t : TopicName.values()) {
                Topic topic = new Topic();
                topic.setName(t);
                topicRepository.save(topic);
            }
        }
    }
}