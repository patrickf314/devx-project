package de.badminton.neubiberg.db.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Data;

@Data
@Entity
public class GenericTestEntity<T> {

    @Id
    int id;

    @Version
    int version;

    T value;

}
