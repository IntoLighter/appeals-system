package com.intolighter.appealssystem.web.assemblers;

import com.intolighter.appealssystem.web.controllers.AppealController;
import com.intolighter.appealssystem.web.controllers.UserController;
import com.intolighter.appealssystem.persistence.models.Appeal;
import lombok.val;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@SuppressWarnings("NullableProblems")
@Component
public class AppealModelAssembler implements RepresentationModelAssembler<Appeal, EntityModel<Appeal>> {

    @Override
    public EntityModel<Appeal> toModel(Appeal appeal) {
        val userId = appeal.getUser().getId();
        return EntityModel.of(appeal,
                linkTo(methodOn(AppealController.class).findAppeal(userId, appeal.getId())).withSelfRel(),
                linkTo(methodOn(AppealController.class).findAllAppeals(userId)).withRel("appeals"),
                linkTo(methodOn(AppealController.class).findAllArchivedAppeals(userId)).withRel("archived_appeals"),
                linkTo(methodOn(UserController.class).findUser(userId)).withRel("user"));
    }
}
