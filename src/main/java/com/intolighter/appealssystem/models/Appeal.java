package com.intolighter.appealssystem.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@Table(name = "appeals")
public class Appeal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User user;

    @Setter(AccessLevel.NONE)
    private Date requestTime = new Date();

    @Size(max = 20)
    @Lob
    private String classifier;
    private String description;

    private boolean archived = false;

    public Appeal(long id, Date requestTime, String classifier, String description, boolean archived) {
        this.id = id;
        this.requestTime = requestTime;
        this.classifier = classifier;
        this.description = description;
        this.archived = archived;
    }
}
