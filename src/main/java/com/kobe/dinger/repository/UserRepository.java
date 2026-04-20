package com.kobe.dinger.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kobe.dinger.model.User;

/*Self reminder: By extending JpaRepository, the following methods are inherited for free:
    - findById(ID id) → Optional<T>
    - findAll() → List<T>
    - save(S entity) → S
    - deleteById(ID id) → void
    - existsById(ID id) → boolean
    - count() → long
Spring generates the implementation at runtime which means no code needed.
*/

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    public Optional<User> findByEmail(String email);
}
