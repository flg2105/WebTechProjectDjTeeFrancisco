package com.projectpulse.backend.team.domain;

import com.projectpulse.backend.user.domain.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "team_memberships")
public class TeamMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private SeniorDesignTeam team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeamMembershipRole membershipRole;

    @Column(nullable = false)
    private OffsetDateTime assignedAt;

    @PrePersist
    public void onCreate() {
        assignedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public SeniorDesignTeam getTeam() {
        return team;
    }

    public void setTeam(SeniorDesignTeam team) {
        this.team = team;
    }

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public TeamMembershipRole getMembershipRole() {
        return membershipRole;
    }

    public void setMembershipRole(TeamMembershipRole membershipRole) {
        this.membershipRole = membershipRole;
    }
}
