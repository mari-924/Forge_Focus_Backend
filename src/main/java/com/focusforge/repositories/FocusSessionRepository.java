package com.focusforge.repositories;

import com.focusforge.models.FocusSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {

    // if you want to use hostId
    List<FocusSession> findByHostIdAndIsPrev(Long hostId, Boolean isPrev);

    // OR directly by host email
    List<FocusSession> findByHostEmailAndIsPrev(String email, Boolean isPrev);
}