package com.example.application.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "magazines")
public class Magazine {

    @Id
    private Long itemId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(nullable = false)
    private String isni;

    @Column(nullable = false)
    private String month;

    @Temporal(TemporalType.DATE)
    @Column(name = "publication_date", nullable = false)
    private Date publicationDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getIsni() {
        return isni;
    }

    public void setIsni(String isni) {
        this.isni = isni;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Magazine() {
    }

    public Magazine(Long itemId, Item item, String isni, String month, Date publicationDate, Date createdAt, Date updatedAt) {
        this.itemId = itemId;
        this.item = item;
        this.isni = isni;
        this.month = month;
        this.publicationDate = publicationDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
