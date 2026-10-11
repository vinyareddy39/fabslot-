package com.fabslot.dto;

public class MachineRequest {

    private String name;
    private String type;     // CNC_ROUTER, THREE_D_PRINTER, LASER_CUTTER, OTHER
    private String status;   // AVAILABLE, MAINTENANCE, RETIRED
    private String location;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
