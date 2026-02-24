package com.java.dospring.model;

import java.io.Serializable;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seats implements Serializable{
private int seat_id;
private String status;
private int capacity;
private int rate;	
}
