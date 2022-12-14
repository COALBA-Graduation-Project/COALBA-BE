package com.project.coalba.domain.substituteReq.entity;

import com.project.coalba.domain.profile.entity.Boss;
import com.project.coalba.domain.profile.entity.Staff;
import com.project.coalba.domain.schedule.entity.Schedule;
import com.project.coalba.domain.substituteReq.entity.enums.SubstituteReqStatus;
import com.project.coalba.global.audit.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;

@Getter @Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class SubstituteReq extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "substitute_req_id")
    private Long id;

    @Column(nullable = false, length = 150)
    private String reqMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubstituteReqStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private Staff receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private Staff sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boss_id")
    private Boss boss;

    public Boolean isWaiting() {
        if (this.status.equals(SubstituteReqStatus.WAITING)) {
            return true;
        } else {
            return false;
        }
    }

    public void cancel() {
        this.status = SubstituteReqStatus.CANCELLATION;
    }

    public void accept() {
        this.status = SubstituteReqStatus.ACCEPTANCE;
    }

    public void refuse() {
        this.status = SubstituteReqStatus.REFUSAL;
    }

    public void approve() {
        this.status = SubstituteReqStatus.APPROVAL;
    }

    public void disapprove() {
        this.status = SubstituteReqStatus.DISAPPROVAL;
    }


}
