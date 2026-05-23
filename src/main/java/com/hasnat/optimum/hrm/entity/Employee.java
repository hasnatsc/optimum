package com.hasnat.optimum.hrm.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.organization.entity.Department;
import com.hasnat.optimum.organization.entity.Organization;
import com.hasnat.optimum.security.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "hrm_employees")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Employee extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String employeeCode;

    @Column(nullable = false, length = 100) private String firstName;
    @Column(length = 100)                   private String middleName;
    @Column(nullable = false, length = 100) private String lastName;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Gender gender;

    @Column(nullable = false) private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING) @Column(length = 20)
    private BloodGroup bloodGroup;

    @Enumerated(EnumType.STRING) @Column(length = 20)
    private MaritalStatus maritalStatus;

    @Column(unique = true, length = 50)  private String nationalId;
    @Column(unique = true, length = 50)  private String passportNumber;
    @Column(nullable = false, unique = true, length = 20) private String phone;
    @Column(length = 20)   private String alternatePhone;
    @Column(length = 100)  private String email;

    @Column(length = 100) private String emergencyContactName;
    @Column(length = 20)  private String emergencyContactPhone;
    @Column(length = 100) private String emergencyContactRelation;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private EmployeeType employeeType;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private EmployeeStatus status;

    @Column(nullable = false) private LocalDate joiningDate;
    private LocalDate confirmationDate;
    private LocalDate probationEndDate;
    private LocalDate resignationDate;
    private LocalDate exitDate;

    @Column(length = 50) private String workLocation;
    @Column(length = 50) private String workShift;

    @Column(precision = 12, scale = 2) private BigDecimal basicSalary;
    @Column(precision = 12, scale = 2) private BigDecimal grossSalary;
    @Column(length = 50) private String bankName;
    @Column(length = 50) private String bankBranch;
    @Column(length = 50) private String bankAccountNumber;

    private Integer annualLeaveDays;
    private Integer casualLeaveDays;
    private Integer sickLeaveDays;

    @Column(length = 255)  private String profilePicture;
    @Column(length = 1000) private String notes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "designation_id", nullable = false)
    private Designation designation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporting_manager_id")
    private Employee reportingManager;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    public enum Gender { MALE, FEMALE, OTHER }
    public enum BloodGroup { A_POSITIVE, A_NEGATIVE, B_POSITIVE, B_NEGATIVE, O_POSITIVE, O_NEGATIVE, AB_POSITIVE, AB_NEGATIVE }
    public enum MaritalStatus { SINGLE, MARRIED, DIVORCED, WIDOWED }
    public enum EmployeeType { PERMANENT, CONTRACT, TEMPORARY, INTERN, PART_TIME, CONSULTANT }
    public enum EmployeeStatus { ACTIVE, INACTIVE, ON_LEAVE, SUSPENDED, TERMINATED, RESIGNED, RETIRED }
}
