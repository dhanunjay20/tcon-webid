package com.tcon.webid.repository;

import com.tcon.webid.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByMobile(String mobile);
    Optional<User> findByMobileIn(List<String> mobiles);
    boolean existsByEmail(String email);
    boolean existsByMobile(String mobile);
}
