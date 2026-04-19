package br.leetjourney.taskmanager.mapper;

import br.leetjourney.taskmanager.domain.model.User;
import br.leetjourney.taskmanager.dto.response.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
