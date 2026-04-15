package com.medisphere.admin.repository;

import com.medisphere.admin.entity.MedisphereAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminRepository extends JpaRepository<MedisphereAdmin, Integer> {
    List<MedisphereAdmin> findByStatus(String status);
    long countByStatus(String status);
}
