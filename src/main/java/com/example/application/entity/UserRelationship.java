package com.example.application.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "user_relationships")
@IdClass(UserRelationshipId.class)
public class UserRelationship implements Serializable {

    @Id
    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @Id
    @ManyToOne
    @JoinColumn(name = "child_id", nullable = false)
    private User child;

    @Column(name = "relationship_type", nullable = false)
    private String relationshipType;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;
}
