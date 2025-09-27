package org.linker.plnm.domain.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @OneToMany(mappedBy = "chatGroup", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Team> teams;
}
