package com.intolighter.appealssystem.web.controllers;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/users/{id}")
public class UserController {

    @GetMapping
    public ResponseEntity<?> findUser(@PathVariable long id) {
        RepresentationModel<?> resource = new RepresentationModel<>();
        resource.add(
                linkTo(methodOn(UserController.class).findUser(id)).withSelfRel(),
                linkTo(methodOn(AppealController.class).findAllAppeals(id)).withRel("appeals"),
                linkTo(methodOn(AppealController.class).findAllArchivedAppeals(id)).withRel("archived_appeals"));

        return ResponseEntity.ok(resource);
    }
}
