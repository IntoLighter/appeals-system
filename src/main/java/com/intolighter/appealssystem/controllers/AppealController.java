package com.intolighter.appealssystem.controllers;

import com.intolighter.appealssystem.assemblers.AppealModelAssembler;
import com.intolighter.appealssystem.errors.exceptions.AppealAlreadyExistsException;
import com.intolighter.appealssystem.errors.exceptions.AppealNotFoundException;
import com.intolighter.appealssystem.models.Appeal;
import com.intolighter.appealssystem.repositories.AppealRepository;
import com.intolighter.appealssystem.repositories.UserRepository;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/users/{userId}/appeals")
public class AppealController {

    private final AppealModelAssembler assembler;
    private final UserRepository userRepository;
    private final AppealRepository appealRepository;

    private static Logger logger = LoggerFactory.getLogger(AppealController.class);

    public AppealController(AppealModelAssembler assembler,
                            UserRepository userRepository,
                            AppealRepository appealRepository) {
        this.assembler = assembler;
        this.userRepository = userRepository;
        this.appealRepository = appealRepository;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Appeal>>> findAllAppeals(
            @PathVariable long userId,
            @RequestParam(required = false, defaultValue = "false") boolean archived) {
        val role =
                SecurityContextHolder.getContext().getAuthentication().getAuthorities().toArray()[0].toString();
        List<Appeal> appeals;
        if (role.equals("ROLE_GOVERNMENT")) {
            appeals = appealRepository.findAll(archived);
        } else if (role.equals("ROLE_USER")) {
            appeals = appealRepository.findAllByUserId(userId, archived);
        } else {
            throw new RuntimeException("User role " + role + " is not found");
        }

        return ResponseEntity.ok(assembler.toCollectionModel(appeals));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Appeal>> findAppeal(@PathVariable long id, @PathVariable long userId) {
        val employee = appealRepository.findById(id)
                .orElseThrow(() -> new AppealNotFoundException(id));

        return ResponseEntity.ok(assembler.toModel(employee));
    }

    @PostMapping
    public ResponseEntity<?> createAppeal(@Valid @RequestBody Appeal appeal, @PathVariable long userId) {
        if (appealRepository.existsByDescription(appeal.getDescription())) {
            throw new AppealAlreadyExistsException(
                    "There is an appeal with the description: '" + appeal.getDescription() + "'");
        }

        appeal.setUser(userRepository.findById(userId).orElseThrow(
                () -> new UsernameNotFoundException("User with id " + userId + " is not found")));


        val newAppeal = appealRepository.save(appeal);

        return ResponseEntity
                .created(linkTo(methodOn(AppealController.class).findAppeal(newAppeal.getId(), userId)).withSelfRel().toUri())
                .body(assembler.toModel(newAppeal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> replaceAppeal(
            @Valid @RequestBody Appeal appeal, @PathVariable long id, @PathVariable long userId) {
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppeal(@PathVariable long id, @PathVariable long userId) {
        appealRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
