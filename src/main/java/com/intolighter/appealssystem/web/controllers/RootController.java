package com.intolighter.appealssystem.web.controllers;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class RootController {

    @GetMapping
    public ResponseEntity<?> root() {
        RepresentationModel<?> resource = new RepresentationModel<>();

        resource.add(
                linkTo(methodOn(RootController.class).root()).withSelfRel(),
                linkTo(AuthController.class).withRel("auth"));

        return ResponseEntity.ok(resource);
    }
}
