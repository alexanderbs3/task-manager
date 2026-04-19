package br.leetjourney.taskmanager.mapper;

import br.leetjourney.taskmanager.domain.model.Task;
import br.leetjourney.taskmanager.dto.response.TaskResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaskMapper {


    TaskResponse toResponse(Task task);
}
