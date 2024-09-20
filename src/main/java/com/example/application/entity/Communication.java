package com.example.application.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "communications")
public class Communication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_type", nullable = false)
    private String messageType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Temporal(TemporalType.DATE)
    @Column(name = "send_date", nullable = false)
    private Date sendDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    // Relation avec User (Member)
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    public Communication() {
    }

    public Communication(Long id, String messageType, String content, Date sendDate, Date createdAt, Date updatedAt, User member) {
        this.id = id;
        this.messageType = messageType;
        this.content = content;
        this.sendDate = sendDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.member = member;
    }
}
