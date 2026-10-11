package com.fabslot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fabslot.entity.UsageLog;

public interface UsageLogRepository extends JpaRepository<UsageLog, Long> {
}
