package lee.joohan.whattoeattelegrambot.handler.bot;

import static lee.joohan.whattoeattelegrambot.common.ResponseMessage.RANDOM_PICK_ARGS_ERROR_RESPONSE;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lee.joohan.whattoeattelegrambot.common.ResponseMessage;
import lee.joohan.whattoeattelegrambot.domain.Cafe;
import lee.joohan.whattoeattelegrambot.domain.User;
import lee.joohan.whattoeattelegrambot.domain.telegram.TelegramMessage;
import lee.joohan.whattoeattelegrambot.service.CafeService;
import lee.joohan.whattoeattelegrambot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * Created by Joohan Lee on 2020/03/12
 */


@Component
@RequiredArgsConstructor
public class CafeBotCommandHandler {
  private final UserService userService;
  private final CafeService cafeService;

  public Mono<String> addCafe(TelegramMessage message) {
    if (!Pattern.matches("/\\S+ \\S+", message.getText())) {
      return Mono.just(ResponseMessage.REGISTER_CAFE_ARGS_ERROR_RESPONSE);
    }

    return userService.getOrRegister(
        User.builder()
            .firstName(message.getFrom().getFirstName())
            .lastName(message.getFrom().getLastName())
            .telegramId(message.getFrom().getId())
            .build())
        .flatMap(it -> cafeService.register(it, message.getText().split(" ")[1]))
        .map(it -> ResponseMessage.REGISTER_CAFE_RESPONSE);
  }

  @Transactional(readOnly = true)
  public Mono<String> list() {
    return cafeService.getAll()
        .map(Cafe::getName)
        .sort()
        .collect(Collectors.joining(",\n"));
  }

  @Transactional(readOnly = true)
  public Mono<String> random(TelegramMessage telegramMessage) {
    String[] input = telegramMessage.getText().split(" ");

    int num = input.length > 1 ? Integer.parseInt(input[1]) : 1;

    return cafeService.randomSample(num)
        .map(Cafe::getName)
        .sort()
        .collect(Collectors.joining("\n"))
        .onErrorReturn(IllegalArgumentException.class, RANDOM_PICK_ARGS_ERROR_RESPONSE)
        .onErrorReturn(NumberFormatException.class, RANDOM_PICK_ARGS_ERROR_RESPONSE);
  }
}