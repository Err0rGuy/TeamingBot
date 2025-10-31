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
    private Long id;

    @Column(unique = true, nullable = false)
    private String userName;

    private String firstName;

    private String lastName;

    @Builder.Default
    @ManyToMany(mappedBy = "members", cascade = {CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<Team> teams = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;
        return id != null && id.equals(((Member) o).id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }


    public String getDisplayName() {
        return this.userName != null
                ? "@" + this.userName
                : this.firstName != null
                ? this.firstName
                : this.id.toString();
    }
}