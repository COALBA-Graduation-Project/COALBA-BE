package com.project.coalba.domain.workspace.entity;

import com.project.coalba.domain.profile.entity.Staff;
import com.project.coalba.global.audit.BaseTimeEntity;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Getter @Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class WorkspaceMember extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workspace_member_id")
    private Long id;

    @ColumnDefault("9160")
    @Column(nullable = false)
    private Integer hourlyWage;

    @ColumnDefault("100")
    @Column(nullable = false, columnDefinition = "int CHECK (work_grade >= 0 AND work_grade <= 100)")
    private Integer workGrade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;
}
