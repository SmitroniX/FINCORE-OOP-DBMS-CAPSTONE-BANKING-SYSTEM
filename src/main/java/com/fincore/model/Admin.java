package com.fincore.model;

import java.time.LocalDateTime;

/**
 * Admin class extending User, representing bank officers and system administrators.
 */
public class Admin extends User {

    private String department;
    private int clearanceLevel;

    public Admin() {
        super();
    }

    public Admin(Long id, String adminCode, String name, String email, String phone, String department, int clearanceLevel) {
        super(id, adminCode, name, email, phone, "ACTIVE", LocalDateTime.now());
        this.department = department;
        this.clearanceLevel = clearanceLevel;
    }

    @Override
    public String getRoleDescription() {
        return "Bank Operations Administrator (Dept: " + department + ", Clearance L" + clearanceLevel + ")";
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getClearanceLevel() {
        return clearanceLevel;
    }

    public void setClearanceLevel(int clearanceLevel) {
        this.clearanceLevel = clearanceLevel;
    }
}
