package org.linker.plnm.domain.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatGroup {
    @Id
    private Long chatId;

    private String name;

    @Builder.Default
    @OneToMany(mappedBy = "chatGroup", cascade = {CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<Team> teams =  new HashSet<>();
}
