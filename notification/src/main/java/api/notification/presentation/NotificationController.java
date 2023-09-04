package api.notification.presentation;

import api.notification.application.NotificationService;
import api.notification.domain.Notification;
import api.notification.presentation.mapper.NotificationMapper;
import api.notification.presentation.model.NotificationModel;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.net.URI;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/notification")
public class NotificationController
{
    @Autowired(required = true)
    private NotificationService notificationService;
    @Autowired
    private NotificationMapper notificationMapper;

    @Operation(summary = "Listar todos los monederos Notification registrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se listaron todos los monederos Notification registrados",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Notification.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @GetMapping("/findAll")
    @CircuitBreaker(name = "yankiCircuit", fallbackMethod = "fallbackGetAllNotification")
    @TimeLimiter(name = "yankiTimeLimiter")
    @Timed(description = "yankiGetAll")
    public Flux<NotificationModel> getAll() {
        log.info("getAll executed");
        return notificationService.findAll()
                .map(yanki -> notificationMapper.entityToModel(yanki));
    }


    @Operation(summary = "Listar todos los monederos Notification por Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se listaron todos los monederos yanki por Id",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Notification.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @GetMapping("/findById/{id}")
    @CircuitBreaker(name = "yankiCircuit", fallbackMethod = "fallbackFindById")
    @TimeLimiter(name = "yankiTimeLimiter")
    @Timed(description = "yankisGetById")
    public Mono<ResponseEntity<NotificationModel>> findById(@PathVariable String id){
        return notificationService.findById(id)
                .map(yanki -> notificationMapper.entityToModel(yanki))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar todos los registros por DNI")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se listaron todos los registros por DNI",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Notification.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @GetMapping("/findByIdentityDni/{identityDni}")
    @CircuitBreaker(name = "yankiCircuit", fallbackMethod = "fallbackFindByIdentityDni")
    @TimeLimiter(name = "yankiTimeLimiter")
    public Mono<ResponseEntity<NotificationModel>> findByIdentityDni(@PathVariable String identityDni){
        log.info("findByIdentityDni executed {}", identityDni);
        Mono<Notification> response = notificationService.findByIdentityDni(identityDni);
        return response
                .map(yanki -> notificationMapper.entityToModel(yanki))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Registro de los Monederos Notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se registro el monedero de manera exitosa",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Notification.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @PostMapping
    @CircuitBreaker(name = "yankiCircuit", fallbackMethod = "fallbackCreateNotification")
    @TimeLimiter(name = "yankiTimeLimiter")
    public Mono<ResponseEntity<NotificationModel>> create(@Valid @RequestBody NotificationModel request){
        log.info("create executed {}", request);
        return notificationService.create(notificationMapper.modelToEntity(request))
                .map(yanki -> notificationMapper.entityToModel(yanki))
                .flatMap(c -> Mono.just(ResponseEntity.created(URI.create(String.format("http://%s:%s/%s/%s", "register", "9081", "yanki", c.getId())))
                        .body(c)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar el monedero Notification por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se actualizará el registro por el ID",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Notification.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @PutMapping("/{id}")
    @CircuitBreaker(name = "yankiCircuit", fallbackMethod = "fallbackUpdateNotification")
    @TimeLimiter(name = "yankiTimeLimiter")
    public Mono<ResponseEntity<NotificationModel>> updateById(@PathVariable String id, @Valid @RequestBody NotificationModel request){
        log.info("updateById executed {}:{}", id, request);
        return notificationService.update(id, notificationMapper.modelToEntity(request))
                .map(yanki -> notificationMapper.entityToModel(yanki))
                .flatMap(c -> Mono.just(ResponseEntity.created(URI.create(String.format("http://%s:%s/%s/%s", "register", "9081", "yanki", c.getId())))
                        .body(c)))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @Operation(summary = "Eliminar Monedero Notification por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se elimino el registro por ID",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Notification.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @DeleteMapping("/{id}")
    @CircuitBreaker(name = "yankiCircuit", fallbackMethod = "fallbackDeleteNotification")
    @TimeLimiter(name = "yankiTimeLimiter")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable String id){
        log.info("deleteById executed {}", id);
        return notificationService.delete(id)
                .map( r -> ResponseEntity.ok().<Void>build())
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
