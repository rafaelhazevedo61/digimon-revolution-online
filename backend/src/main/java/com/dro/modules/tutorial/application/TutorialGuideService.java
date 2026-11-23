package com.dro.modules.tutorial.application;

import com.dro.modules.tutorial.api.dto.response.TutorialGuideResponse;
import com.dro.modules.tutorial.domain.TutorialGuideTopic;
import com.dro.modules.tutorial.infra.TutorialGuideRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TutorialGuideService {
    private final TutorialService tutorial;
    private final TutorialGuideRepository repository;
    private final PlayerRepository players;
    public TutorialGuideService(TutorialService tutorial, TutorialGuideRepository repository, PlayerRepository players) {
        this.tutorial = tutorial; this.repository = repository; this.players = players;
    }
    @Transactional(readOnly = true)
    public TutorialGuideResponse page(String token) {
        var playerId = TokenExtractor.extractPlayerId(token);
        if (!players.existsById(playerId)) throw new NotFoundException("Jogador não encontrado.");
        var initial = tutorial.getProgress(token);
        Set<String> completed = initial.steps().stream().filter(step -> step.completed())
                .map(step -> step.step()).collect(Collectors.toSet());
        Set<String> read = repository.readTopics(playerId);
        var topics = Arrays.stream(TutorialGuideTopic.values()).map(topic -> new TutorialGuideResponse.Topic(
                topic.name(), topic.getGroup(), topic.getTitle(), topic.getDescription(), topic.getRoute(),
                initial.finished() || completed.contains(topic.getPrerequisite().name()),
                "Disponível após: " + topic.getPrerequisite().getTitle(), read.contains(topic.name()))).toList();
        return new TutorialGuideResponse(initial, (int) topics.stream().filter(topic -> topic.read()).count(), topics.size(), topics);
    }
    @Transactional
    public TutorialGuideResponse acknowledge(String token, String code) {
        TutorialGuideTopic topic;
        try { topic = TutorialGuideTopic.valueOf(code.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException | NullPointerException exception) { throw new BadRequestException("Conteúdo do guia desconhecido."); }
        var page = page(token);
        var entry = page.topics().stream().filter(item -> item.code().equals(topic.name())).findFirst().orElseThrow();
        if (!entry.available()) throw new BadRequestException(entry.unlockHint());
        repository.acknowledge(TokenExtractor.extractPlayerId(token), topic.name());
        return page(token);
    }
}
