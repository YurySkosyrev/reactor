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
             // Produser активный и сам через subscription скидывает сообщения
             // Ex massenger очередей, либо внешние hookи прилетают

                .create(sink -> {
                    telegramProducer.subscribe(new BaseSubscriber<Object>() {
                        @Override
                        protected void hookOnNext(Object value) {
                            sink.next(value);
                        }

                        @Override
                        protected void hookOnComplete() {
                            sink.complete();
                        }
                    });

                    // Здесь делаем pull из внешнего сервиса
                    sink.onRequest(r -> {
                        sink.next("DB returns:" + telegramProducer.blockFirst());
                    });
                });

        Flux<String> second = Flux
                .just("World", "Coder")
                .repeat();

        Flux<String> sumFlux = Flux
                .just("hello", "dru", "java", "Linus", "Asia")
                .zipWith(second, (f, s) -> String.format("%s %s", f, s));

        Flux<String> stringFlux = sumFlux
                .delayElements(Duration.ofMillis(1300))
                .timeout(Duration.ofSeconds(1)) // обрываем через 1 секунду и вываливается ошибка
                .retry(3) // попробовать 3 раза
//                .onErrorReturn("Too slow") // выводим сообщение вместо ошибки
                .onErrorResume(throwable ->
                        Flux
                                .interval(Duration.ofMillis(300))
                                .map(String::valueOf))
                .skip(2)
                .take(4);

        stringFlux.subscribe(
                v -> System.out.println(v),

                e -> System.err.println(e),
                () -> System.out.println("finished")
        );
        Thread.sleep(5000l);

//        stringFlux.toIterable(); можно преобразовать к любому типу

    }
}
