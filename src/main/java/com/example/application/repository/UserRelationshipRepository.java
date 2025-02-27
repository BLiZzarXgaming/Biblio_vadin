package com.example.application.repository;

import com.example.application.entity.UserRelationship;
import com.example.application.entity.UserRelationshipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserRelationshipRepository extends JpaRepository<UserRelationship, UserRelationshipId> {

    /**
     * Find all relationships where the specified user is the child
     */
    List<UserRelationship> findByChildId(Long childId);

    /**
     * Find all relationships where the specified user is the parent
     */
    List<UserRelationship> findByParentId(Long parentId);

    /**
     * Find a specific parent-child relationship
     */
    UserRelationship findByParentIdAndChildId(Long parentId, Long childId);

    /**
     * Delete all relationships where the specified user is the child
     */
    @Modifying
    @Transactional
    void deleteByChildId(Long childId);

    /**
     * Delete a specific parent-child relationship
     */
    @Modifying
    @Transactional
    void deleteByParentIdAndChildId(Long parentId, Long childId);

    /**
     * Find all parent users of a child
     */
    @Query("SELECT ur.parent FROM UserRelationship ur WHERE ur.child.id = :childId")
    List<com.example.application.entity.User> findParentsByChildId(@Param("childId") Long childId);
}