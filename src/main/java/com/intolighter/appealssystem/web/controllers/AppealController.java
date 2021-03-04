package com.intolighter.appealssystem.web.controllers;

import com.intolighter.appealssystem.web.assemblers.AppealModelAssembler;
import com.intolighter.appealssystem.web.errors.exceptions.AppealAlreadyExistsException;
import com.intolighter.appealssystem.web.errors.exceptions.AppealNotFoundException;
import com.intolighter.appealssystem.web.errors.exceptions.UserNotFoundException;
import com.intolighter.appealssystem.persistence.models.Appeal;
import com.intolighter.appealssystem.persistence.repositories.AppealRepository;
import com.intolighter.appealssystem.persistence.repositories.UserRepository;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
@RequestMapping("/users/{userId}/appeals")
@PreAuthorize("isAuthenticated()")
public class AppealController {

    private static Logger logger = LoggerFactory.getLogger(AppealController.class);

    private final AppealModelAssembler assembler;
    private final UserRepository userRepository;
    private final AppealRepository appealRepository;

    public AppealController(AppealModelAssembler assembler,
                            UserRepository userRepository,
                            AppealRepository appealRepository) {
        this.assembler = assembler;
        this.userRepository = userRepository;
        this.appealRepository = appealRepository;
    }

    @GetMapping
    public CollectionModel<EntityModel<Appeal>> findAllAppeals(
            @PathVariable long userId) {
        return getAppeals(userId, false);
    }

    @GetMapping("/archived")
    public CollectionModel<EntityModel<Appeal>> findAllArchivedAppeals(
            @PathVariable long userId) {
        return getAppeals(userId, true);
    }

    @GetMapping("/{id}")
    public EntityModel<Appeal> findAppeal(@PathVariable long userId, @PathVariable long id) {
        val employee = appealRepository.findById(id)
                .orElseThrow(() -> new AppealNotFoundException(id));

        return assembler.toModel(employee);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<?> createAppeal(@Valid @RequestBody Appeal appeal, @PathVariable long userId) {
        if (appealRepository.existsByDescription(appeal.getDescription())) {
            throw new AppealAlreadyExistsException(
                    "There is an appeal with the description: '" + appeal.getDescription() + "'");
        }

        appeal.setUser(userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User with id " + userId + " is not found")));

        val newAppeal = appealRepository.save(appeal);

        return ResponseEntity
                .created(linkTo(methodOn(AppealController.class)
                        .findAppeal(userId, appeal.getId())).withSelfRel().toUri())
                .body(assembler.toModel(newAppeal));
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{id}")
    public ResponseEntity<?> replaceAppeal(
            @Valid @RequestBody Appeal appeal, @PathVariable long userId, @PathVariable long id) {
        val _appeal = appealRepository.findById(id)
                .orElseThrow(() -> new AppealNotFoundException(id));

        _appeal.setClassifier(appeal.getClassifier());
        _appeal.setDescription(appeal.getDescription());
        appealRepository.update(_appeal);

        val entityModel = assembler.toModel(_appeal);
        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppeal(@PathVariable long id, @PathVariable long userId) {
        appealRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private CollectionModel<EntityModel<Appeal>> getAppeals(@PathVariable long userId, boolean archived) {
        val role = getRole();
        List<Appeal> appeals;
        if (role.equals("ROLE_GOVERNMENT")) {
            appeals = appealRepository.findAll(archived);
        } else if (role.equals("ROLE_USER")) {
            appeals = appealRepository.findAllByUserId(userId, archived);
        } else {
            throw new UserNotFoundException("User role " + role + " is not found");
        }

        return assembler.toCollectionModel(appeals);
    }

    private String getRole() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().toArray()[0].toString();
    }
}
