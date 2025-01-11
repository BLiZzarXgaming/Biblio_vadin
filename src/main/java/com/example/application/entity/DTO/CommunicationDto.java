package com.example.application.entity.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * DTO for {@link com.example.application.entity.Communication}
 */
public class CommunicationDto implements Serializable {
    private Long id;
    @NotNull
    private UserDto member;
    @NotNull
    @Size(max = 255)
    private String messageType;
    @NotNull
    private String content;
    @NotNull
    private LocalDate sendDate;
    private Instant createdAt;
    private Instant updatedAt;

    public CommunicationDto() {
    }

    public CommunicationDto(Long id, UserDto member, String messageType, String content, LocalDate sendDate, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.member = member;
        this.messageType = messageType;
        this.content = content;
        this.sendDate = sendDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserDto getMember() {
        return member;
    }

    public void setMember(UserDto member) {
        this.member = member;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDate getSendDate() {
        return sendDate;
    }

    public void setSendDate(LocalDate sendDate) {
        this.sendDate = sendDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommunicationDto entity = (CommunicationDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.member, entity.member) &&
                Objects.equals(this.messageType, entity.messageType) &&
                Objects.equals(this.content, entity.content) &&
                Objects.equals(this.sendDate, entity.sendDate) &&
                Objects.equals(this.createdAt, entity.createdAt) &&
                Objects.equals(this.updatedAt, entity.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, member, messageType, content, sendDate, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "member = " + member + ", " +
                "messageType = " + messageType + ", " +
                "content = " + content + ", " +
                "sendDate = " + sendDate + ", " +
                "createdAt = " + createdAt + ", " +
                "updatedAt = " + updatedAt + ")";
    }
}