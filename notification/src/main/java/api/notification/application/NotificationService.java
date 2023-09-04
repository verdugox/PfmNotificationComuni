package api.notification.application;

import api.notification.config.CircuitResilienceListener;
import api.notification.domain.Notification;
import api.notification.domain.NotificationRepository;
import api.notification.presentation.mapper.NotificationMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@ConditionalOnProperty(name = "cache.enabled", havingValue = "true")
public class NotificationService
{
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private CircuitResilienceListener circuitResilienceListener;
    @Autowired
    private TimeLimiterRegistry timeLimiterRegistry;
    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private ReactiveHashOperations<String, String, Notification> hashOperations;

    @CircuitBreaker(name = "notificationCircuit", fallbackMethod = "fallbackGetAllNotification")
    @TimeLimiter(name = "notificationTimeLimiter")
    public Flux<Notification> findAll(){
        log.debug("findAll executed");

        // Intenta obtener todos los monederos notification desde el caché de Redis
        Flux<Notification> cachedNotification = hashOperations.values("NotificationRedis")
                .flatMap(notification -> Mono.justOrEmpty((Notification) notification));

        // Si hay datos en la caché de Redis, retornarlos
        return cachedNotification.switchIfEmpty(notificationRepository.findAll()
                .flatMap(notification -> {
                    // Almacena cada monedero notification en la caché de Redis
                    return hashOperations.put("NotificationRedis", notification.getId(), notification)
                            .thenReturn(notification);
                }));

    }

    @CircuitBreaker(name = "notificationCircuit", fallbackMethod = "fallbackFindById")
    @TimeLimiter(name = "notificationTimeLimiter")
    public Mono<Notification> findById(String notificationId)
    {
        log.debug("findById executed {}" , notificationId);
        return  hashOperations.get("NotificationRedis",notificationId)
                .switchIfEmpty(notificationRepository.findById(notificationId)
                        .flatMap(notification -> hashOperations.put("NotificationRedis",notification.getId(),notification)
                                .thenReturn(notification)));
    }

    @CircuitBreaker(name = "notificationCircuit", fallbackMethod = "fallbackGetAllItems")
    @TimeLimiter(name = "notificationTimeLimiter")
    public Mono<Notification> findByIdentityDni(String identityDni){
        log.debug("findByIdentityDni executed {}" , identityDni);
        return notificationRepository.findByIdentityDni(identityDni);
    }

    @CircuitBreaker(name = "notificationCircuit", fallbackMethod = "fallbackFindByIdentityDni")
    @TimeLimiter(name = "notificationTimeLimiter")
    public Mono<Notification> create(Notification notification){
        log.debug("create executed {}",notification);
        notification.setDateRegister(LocalDate.now());
        return notificationRepository.save(notification);
    }

    @CircuitBreaker(name = "notificationCircuit", fallbackMethod = "fallbackUpdateNotification")
    @TimeLimiter(name = "notificationTimeLimiter")
    public Mono<Notification> update(String notificationId, Notification notification){
        log.debug("update executed {}:{}", notificationId, notification);
        return notificationRepository.findById(notificationId)
                .flatMap(dbNotification -> {
                    notification.setDateRegister(dbNotification.getDateRegister());
                    notificationMapper.update(dbNotification, notification);
                    return notificationRepository.save(dbNotification);
                });
    }

    @CircuitBreaker(name = "notificationCircuit", fallbackMethod = "fallbackDeleteNotification")
    @TimeLimiter(name = "notificationTimeLimiter")
    public Mono<Notification>delete(String notificationId){
        log.debug("delete executed {}",notificationId);
        return notificationRepository.findById(notificationId)
                .flatMap(existingNotification -> notificationRepository.delete(existingNotification)
                        .then(Mono.just(existingNotification)));
    }
}
