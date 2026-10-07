package com.example.pharmaaggregatorserver.entity.product;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tm_flavour_master")
public class Flavour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flavour_id")
    private Long flavourId;

    @Column(name = "flavour_name",  nullable = false, unique = true, length = 50)
    private String flavourName;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
