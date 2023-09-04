package api.notification.domain;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface NotificationRepository extends ReactiveMongoRepository<Notification,String>
{
    Mono<Notification> findByIdentityDni(String identityDni);
}
