package com.example.application.service;

import com.example.application.entity.User;
import com.example.application.entity.UserRelationship;
import com.example.application.entity.UserRelationshipId;
import com.example.application.repository.UserRelationshipRepository;
import com.example.application.repository.UserRepositoryV2;
import com.example.application.service.implementation.UserRelationshipService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserRelationshipServiceImpl implements UserRelationshipService {

    private final UserRelationshipRepository userRelationshipRepository;
    private final UserRepositoryV2 userRepository;

    public UserRelationshipServiceImpl(UserRelationshipRepository userRelationshipRepository,
            UserRepositoryV2 userRepository) {
        this.userRelationshipRepository = userRelationshipRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<User> findParentsByChildId(Long childId) {
        return userRelationshipRepository.findParentsByChildId(childId);
    }

    @Override
    public List<User> findChildrenByParentId(Long parentId) {
        return userRelationshipRepository.findByParentId(parentId).stream()
                .map(UserRelationship::getChild)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserRelationship createParentChildRelationship(Long parentId, Long childId, String relationshipType) {
        // Check if users exist
        Optional<User> parentUser = userRepository.findById(parentId);
        Optional<User> childUser = userRepository.findById(childId);

        if (parentUser.isEmpty() || childUser.isEmpty()) {
            throw new IllegalArgumentException("Parent or child user not found");
        }

        // Check if relationship already exists
        UserRelationship existingRelationship = userRelationshipRepository.findByParentIdAndChildId(parentId, childId);
        if (existingRelationship != null) {
            return existingRelationship; // Relationship already exists
        }

        // Create new relationship
        UserRelationship relationship = new UserRelationship();

        // Set the composite key
        UserRelationshipId id = new UserRelationshipId();
        id.setParentId(parentId);
        id.setChildId(childId);
        relationship.setId(id);

        // Set the entities
        relationship.setParent(parentUser.get());
        relationship.setChild(childUser.get());

        // Set the type and timestamps
        relationship.setRelationshipType(relationshipType);
        relationship.setCreatedAt(Instant.now());
        relationship.setUpdatedAt(Instant.now());

        return userRelationshipRepository.save(relationship);
    }

    @Override
    @Transactional
    public void removeAllParentRelationships(Long childId) {
        userRelationshipRepository.deleteByChildId(childId);
    }

    @Override
    @Transactional
    public void updateParentChildRelationships(Long childId, List<Long> parentIds, String relationshipType) {
        // Remove existing relationships
        removeAllParentRelationships(childId);

        // Create new relationships
        for (Long parentId : parentIds) {
            if (parentId != null) {
                createParentChildRelationship(parentId, childId, relationshipType);
            }
        }
    }

    @Override
    public boolean isParentOfChild(Long parentId, Long childId) {
        return userRelationshipRepository.findByParentIdAndChildId(parentId, childId) != null;
    }
}