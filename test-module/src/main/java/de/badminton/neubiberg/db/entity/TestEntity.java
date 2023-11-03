package de.badminton.neubiberg.db.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Collection;
import java.util.Map;

@Data
@Entity
public class TestEntity {

    @Id
    int id;

    @Version
    int version;

    String label;

    @OneToMany(mappedBy = "parent")
    Collection<ChildEntity> children;

    @CollectionTable
    @ElementCollection
    @MapKey(name = "key")
    Map<String, String> map;

}
