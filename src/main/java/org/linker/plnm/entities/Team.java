package org.linker.plnm.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(
    name = "teams",
    uniqueConstraints = {
            @UniqueConstraint(columnNames = {"name", "chat_group_id"})
    }
)
public class Team implements Assignee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToMany(fetch =  FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "team_members",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private Set<Member> members = new HashSet<>();

    @ManyToMany(mappedBy = "teams", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Task> tasks = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "chat_group_id", nullable = false)
    private ChatGroup chatGroup;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Team)) return false;
        return id != null && id.equals(((Team) o).id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}