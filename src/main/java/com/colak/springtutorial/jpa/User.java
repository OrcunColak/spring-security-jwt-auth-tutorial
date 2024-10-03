package com.colak.springtutorial.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")

@Getter
@Setter
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "password", nullable = false)
    private String password;

    // Unidirectional ManyToMany
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_role", // This is the join table
            joinColumns = {@JoinColumn(name = "user_id")}, // User foreign key
            inverseJoinColumns = {@JoinColumn(name = "role_id")} // Role foreign key
    )
    private Set<Role> authorities = new HashSet<>();

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;


    // #############################################
    // overrides part for UserDetails implementation
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public Set<Role> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }
}
