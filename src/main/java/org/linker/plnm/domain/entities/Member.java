package org.linker.plnm.domain.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Member {
    @Id
    private Long telegramId;

    @Column(unique = true, nullable = false)
    private String username;

    private String firstName;

    private String lastName;

    @Builder.Default
    @ManyToMany(mappedBy = "members", cascade = {CascadeType.PERSIST})
    private Set<Team> teams = new HashSet<>();

    @Builder.Default
    @ManyToMany(mappedBy = "members",  cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Task> tasks = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;
        return telegramId != null && telegramId.equals(((Member) o).telegramId);
    }

    @Override
    public int hashCode() {
        return telegramId != null ? telegramId.hashCode() : 0;
    }


    public String getDisplayName() {
        return this.username != null
                ? "@" + this.username
                : this.firstName != null
                ? this.firstName
                : this.telegramId.toString();
    }
}