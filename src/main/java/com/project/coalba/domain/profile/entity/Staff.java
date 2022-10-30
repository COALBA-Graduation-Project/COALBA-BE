package com.project.coalba.domain.profile.entity;

import com.project.coalba.domain.auth.entity.User;
import com.project.coalba.global.audit.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Getter @Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Staff extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String realName;

    @Column(nullable = false, unique = true, length = 11)
    private String phoneNum;

    @Column(nullable = false)
    private LocalDate birthDate;

    private String imageUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}