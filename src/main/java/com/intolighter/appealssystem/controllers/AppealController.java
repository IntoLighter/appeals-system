package com.intolighter.appealssystem.controllers;

import com.intolighter.appealssystem.assemblers.AppealModelAssembler;
import com.intolighter.appealssystem.errors.exceptions.AppealAlreadyExistsException;
import com.intolighter.appealssystem.errors.exceptions.AppealNotFoundException;
import com.intolighter.appealssystem.models.Appeal;
import com.intolighter.appealssystem.repositories.AppealRepository;
import com.intolighter.appealssystem.repositories.UserRepository;
import lombok.val;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/users/{userId}/appeals")
public class AppealController {

    private AppealModelAssembler assembler;
    private UserRepository userRepository;
    private AppealRepository appealRepository;

    public AppealController(AppealModelAssembler assembler,
                            UserRepository userRepository,
                            AppealRepository appealRepository) {
        this.assembler = assembler;
        this.userRepository = userRepository;
        this.appealRepository = appealRepository;
    }

    private void setUserIdInAssembler(long id) {
        assembler.userId = id;
    }

//    @PreAuthorize("hasRole('GOVERNMENT')")
//    @GetMapping("")
//    public ResponseEntity<CollectionModel<EntityModel<Appeal>>> findAppeals(
//            @PathVariable long userId, @RequestParam boolean archived) {
//        setUserIdInAssembler(userId);
//        return ResponseEntity.ok(assembler.toCollectionModel(appealRepository.findAll(archived)));
//    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Appeal>>> findAppealsByUserId(
            @PathVariable long userId, @RequestParam(required = false, defaultValue = "false") boolean archived) {
        setUserIdInAssembler(userId);
        return ResponseEntity.ok(assembler.toCollectionModel(appealRepository.findAllByUserId(userId, archived)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Appeal>> findAppeal(@PathVariable long id, @PathVariable long userId) {
        setUserIdInAssembler(userId);
        val employee = appealRepository.findById(id)
                .orElseThrow(() -> new AppealNotFoundException(id));

        return ResponseEntity.ok(assembler.toModel(employee));
    }

    @PostMapping
    public ResponseEntity<?> createAppeal(@Valid @RequestBody Appeal appeal, @PathVariable long userId) {
        setUserIdInAssembler(userId);

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
        setUserIdInAssembler(userId);
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
        setUserIdInAssembler(userId);
        appealRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
