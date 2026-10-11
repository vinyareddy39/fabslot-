package com.fabslot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fabslot.entity.Machine;

public interface MachineRepository extends JpaRepository<Machine, Long> {
}
