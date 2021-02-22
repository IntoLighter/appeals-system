package com.intolighter.appealssystem.assemblers;

import com.intolighter.appealssystem.controllers.AppealController;
import com.intolighter.appealssystem.controllers.UserController;
import com.intolighter.appealssystem.models.Appeal;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@SuppressWarnings("NullableProblems")
@Component
public class AppealModelAssembler implements RepresentationModelAssembler<Appeal, EntityModel<Appeal>> {

    public long userId;

    @Override
    public EntityModel<Appeal> toModel(Appeal appeal) {
        return EntityModel.of(appeal,
                linkTo(methodOn(AppealController.class).findAppeal(appeal.getId(), userId)).withSelfRel(),
                linkTo(methodOn(AppealController.class).findAppealsByUserId(userId, false)).withRel("appeals"),
                linkTo(methodOn(UserController.class).findUser(userId)).withRel("user"));
    }
}
