package com.hasnat.optimum.security.repository;
import com.hasnat.optimum.security.entity.UserContext;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface UserContextRepository extends JpaRepository<UserContext, Long> {
    Optional<UserContext> findByUserId(Long userId);
    // userId is the PK — save/findById covers most use cases
}
