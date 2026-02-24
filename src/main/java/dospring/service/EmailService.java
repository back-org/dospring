package com.java.dospring.service;

import com.java.dospring.model.Booking;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Simple email service.
 * In production, use async sending + templates (Thymeleaf) + retry mechanism.
 */
@Service
public class EmailService {

  private final JavaMailSender javaMailSender;

  public EmailService(JavaMailSender javaMailSender) {
    this.javaMailSender = javaMailSender;
  }

  public void sendBookingConfirmation(Booking booking, String mailId) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(mailId);
    message.setSubject("Booking Confirmation");
    message.setText("Booking confirmed. Booking ID: " + booking.getBookingId()
        + " | Total: " + booking.getTotalAmount());
    javaMailSender.send(message);
  }
}
