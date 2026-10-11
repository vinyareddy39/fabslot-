package com.fabslot.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fabslot.dto.MachineRequest;
import com.fabslot.entity.Machine;
import com.fabslot.entity.MachineStatus;
import com.fabslot.entity.MachineType;
import com.fabslot.repository.MachineRepository;

@Service
public class MachineService {

    private final MachineRepository machineRepository;

    public MachineService(MachineRepository machineRepository) {
        this.machineRepository = machineRepository;
    }

    public List<Machine> findAll() {
        return machineRepository.findAll();
    }

    public Machine findById(Long id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Machine not found"));
    }

    public Machine create(MachineRequest req) {
        Machine machine = new Machine(
                req.getName(),
                MachineType.valueOf(req.getType().toUpperCase()),
                req.getStatus() != null ? MachineStatus.valueOf(req.getStatus().toUpperCase()) : MachineStatus.AVAILABLE,
                req.getLocation());
        return machineRepository.save(machine);
    }

    public Machine update(Long id, MachineRequest req) {
        Machine machine = findById(id);
        machine.setName(req.getName());
        machine.setType(MachineType.valueOf(req.getType().toUpperCase()));
        machine.setStatus(MachineStatus.valueOf(req.getStatus().toUpperCase()));
        machine.setLocation(req.getLocation());
        return machineRepository.save(machine);
    }

    public void delete(Long id) {
        machineRepository.deleteById(id);
    }
}
