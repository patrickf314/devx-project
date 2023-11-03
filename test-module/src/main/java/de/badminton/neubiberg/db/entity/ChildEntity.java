package de.badminton.neubiberg.db.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import lombok.Data;

@Data
@Entity
public class ChildEntity {

    @Id
    int id;

    @Version
    int version;

    String name;

    int age;

    @ManyToOne
    TestEntity parent;
}
