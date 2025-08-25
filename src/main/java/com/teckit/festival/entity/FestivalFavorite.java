package com.teckit.festival.entity;

import com.teckit.festival.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "festival_favorites",
        uniqueConstraints = @UniqueConstraint(name="uq_favorite_fid_user", columnNames={"fid","user_id"}),
        indexes = {
                @Index(name="idx_favorite_user_created", columnList="user_id, created_at"),
                @Index(name="idx_favorite_fid", columnList="fid")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FestivalFavorite extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 공연 식별자
    @Column(name = "fid", nullable = false)
    private String fid;


    // 관심 상품 등록한 사용자 ID
    @Column(name = "user_id", nullable = false)
    private Long userId;
}