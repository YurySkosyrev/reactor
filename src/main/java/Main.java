import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        // Mono и Flux могут быть пустыми
        Mono.empty();
        Flux.empty();

        // Mono и Flux могут состоять из элементов
        Mono<Integer> mono = Mono.just(1);
        Flux<Integer> flux = Flux.just(1, 2, 3);

        // Преобразуются друг в друга
        Flux<Integer> fluxFromMono = mono.flux();
        Mono<Boolean> monoFrom = flux.any(s -> s.equals(1));

        // Получение элемента по индексу
        Mono<Integer> integerMono = flux.elementAt(1);

        // Чтобы получить элементы из Flux нужен подписчик в функцию subscribe() - Consumer
        Flux.range(1,5);
        Flux.fromIterable(Arrays.asList(1,2,3));

        // Альтернатива этому выводу - использование Flux как генератора generate
        // без subscribe() в случае бесконечного вывода не будет запускаться,
        // т.к. все реактивные потоки ленивые по максимуму

        Flux.<String>generate(sink -> {
                    sink.next("hello");
                });


        // Без задержки Thread.sleep основного потока код не сработает, так как основной поток увидит, что выполнять
        // больше нечего и программа завершится, как и поток вывода hello
        Flux.<String>generate(sink -> {
            sink.next("hello");
        })
                .delayElements(Duration.ofMillis(500))
                .take(4);

//        Thread.sleep(4001);

        Flux<Object> telegramProducer = Flux
                .generate(
                        () -> 2354,
                        (state, sink) -> {
                            if (state > 2366) {
                                sink.complete();
                            } else {
                                sink.next("Step: " + state);
                            }
                            return state + 3;
                        }
                );


        Flux // есть аналог push - только он однопоточный
                .create(sink ->
                        telegramProducer.subscribe(new BaseSubscriber<Object>() {
                            @Override
                            protected void hookOnNext(Object value) {
                                sink.next(value);
                            }

                            @Override
                            protected void hookOnComplete() {
                                sink.complete();
                            }
                        })
                )
                .subscribe(System.out::println);

    }
}
