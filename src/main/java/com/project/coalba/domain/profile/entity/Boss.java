package com.project.coalba.domain.profile.entity;

import com.project.coalba.domain.auth.entity.User;
import com.project.coalba.domain.substituteReq.entity.SubstituteReq;
import com.project.coalba.domain.timecardReq.entity.TimecardReq;
import com.project.coalba.domain.workspace.entity.Workspace;
import com.project.coalba.global.audit.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter @Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Boss extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "boss_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String realName;

    @Column(nullable = false, length = 11)
    private String phoneNumber;

    @Column(nullable = false)
    private LocalDate birthDate;

    private String imageUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(nullable = false)
    private String deviceToken;

    @Builder.Default
    @OneToMany(mappedBy = "boss")
    private List<Workspace> workspaceList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "boss")
    private List<SubstituteReq> substituteReqList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "boss")
    private List<TimecardReq> timecardReqList = new ArrayList<>();

    public void mapUser(User user) {
        this.user = user;
    }

    public void update(String realName, String phoneNumber, LocalDate birthDate, String imageUrl) {
        this.realName = realName;
        this.phoneNumber = phoneNumber;
        this.birthDate = birthDate;
        this.imageUrl = imageUrl;
    }
}
