package com.example.application.service.implementation;

import com.example.application.entity.User;
import com.example.application.entity.UserRelationship;
import com.example.application.entity.UserRelationshipId;

import java.util.List;

/**
 * Service interface for managing user relationships
 */
public interface UserRelationshipService {

    /**
     * Get all parents of a child
     */
    List<User> findParentsByChildId(Long childId);

    /**
     * Get all children of a parent
     */
    List<User> findChildrenByParentId(Long parentId);

    /**
     * Create a parent-child relationship
     */
    UserRelationship createParentChildRelationship(Long parentId, Long childId, String relationshipType);

    /**
     * Remove all parent-child relationships for a child
     */
    void removeAllParentRelationships(Long childId);

    /**
     * Update parent-child relationships
     * This will remove existing relationships and create new ones
     */
    void updateParentChildRelationships(Long childId, List<Long> parentIds, String relationshipType);

    /**
     * Check if a user is already a parent of a child
     */
    boolean isParentOfChild(Long parentId, Long childId);
}