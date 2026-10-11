package com.fabslot.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fabslot.dto.ApiResponse;
import com.fabslot.dto.MachineRequest;
import com.fabslot.entity.Machine;
import com.fabslot.service.MachineService;

@RestController
@RequestMapping("/api/machines")
public class MachineController {

    private final MachineService machineService;

    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    // Visible to STUDENT and LAB_ADMIN (see SecurityConfig)
    @GetMapping
    public List<Machine> getAll() {
        return machineService.findAll();
    }

    @GetMapping("/{id}")
    public Machine getOne(@PathVariable Long id) {
        return machineService.findById(id);
    }

    // Only lab admins may create, update, or delete machines
    @PreAuthorize("hasRole('LAB_ADMIN')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody MachineRequest req) {
        try {
            return ResponseEntity.ok(machineService.create(req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('LAB_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody MachineRequest req) {
        try {
            return ResponseEntity.ok(machineService.update(id, req));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('LAB_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        machineService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Machine deleted"));
    }
}
