package br.com.pipocarosa.models;
import jakarta.persistence.*;
import org.hibernate.annotations.Generated;

import java.io.Serializable;
import java.util.UUID;

@Entity(name = "USERS")
public class UserModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Generated
    @Column(insertable = false, updatable = false)
    private UUID uuid;
    private String name;
    private String email;
    private String birthDate;
    private String password;

}
