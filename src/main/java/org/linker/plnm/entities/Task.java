package org.linker.plnm.entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "tasks",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "member_id"})
        }
)
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToMany(fetch =  FetchType.EAGER)
    @JoinTable(
            name = "team_tasks",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "team_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"name", "team_id"})
    )
    private Set<Team> teams = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "member_tasks",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"name", "member_id"})
    )
    private Set<Member> members = new HashSet<>();

    @Column
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.UNDONE;

    public enum TaskStatus {
        UNDONE,
        IN_PROGRESS,
        DONE
    }
}
