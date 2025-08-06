package com.teckit.festival.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "festival_event")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String festivalId;
    private String eventType;
    private String description;
}
