package by.lyofchik.quiz.Model.Mapper;

import by.lyofchik.quiz.Model.DTO.Response.LobbyDTO;
import by.lyofchik.quiz.Model.Entity.Lobby;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LobbyMapper {
    @Mapping(source = "quiz.id", target = "quizId")
    @Mapping(source = "quiz.title", target = "quizTitle")
    @Mapping(target = "numPlayers", expression = "java(lobby.getGameMembers().size())")
    @Mapping(target = "startedAt", expression = "java(lobby.getStartedAt() == null ? null : lobby.getStartedAt().toString())")
    LobbyDTO toDto(Lobby lobby);
}