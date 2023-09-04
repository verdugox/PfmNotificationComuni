package api.notification.presentation.mapper;

import api.notification.domain.Notification;
import api.notification.presentation.model.NotificationModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface NotificationMapper
{
    Notification modelToEntity (NotificationModel model);
    NotificationModel entityToModel(Notification event);
    @Mapping(target = "id", ignore=true)
    void update(@MappingTarget Notification entity, Notification updateEntity);
}
