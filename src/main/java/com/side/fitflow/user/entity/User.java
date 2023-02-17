package com.side.fitflow.user.entity;

import com.side.fitflow.comm.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Entity
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Long userId;

    @Column(unique = true)
    private String userEmail;
    private String userPwd;
    private String userNickname;

    @ManyToMany
    @JoinTable(
            joinColumns = {@JoinColumn(name="user_id",referencedColumnName = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "authority_name",referencedColumnName = "authority_name")}
    )
    private Set<Authority> userAuthorities = new HashSet<>();

    public String getAuthoritiesToString() {
        return this.getUserAuthorities().stream()
                .map(Authority::getAuthorityName)
                .collect(Collectors.joining(","));
    }

    public void addAuthority(Authority authority) {
        this.getUserAuthorities().add(authority);
    }

    public void removeAuthority(Authority authority) {
        this.getUserAuthorities().remove(authority);
    }
}
