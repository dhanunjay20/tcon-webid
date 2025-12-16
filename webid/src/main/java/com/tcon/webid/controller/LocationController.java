package com.tcon.webid.controller;

import com.tcon.webid.dto.LocationUpdateDto;
import com.tcon.webid.entity.User;
import com.tcon.webid.entity.Vendor;
import com.tcon.webid.repository.UserRepository;
import com.tcon.webid.repository.VendorRepository;
import com.tcon.webid.service.ChatNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/location")
public class LocationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private ChatNotificationService chatNotificationService;

    /**
     * Update live location for a user or vendor. Body: { id, latitude, longitude, timestamp? }
     * The controller auto-detects whether the id belongs to a User or Vendor by checking repositories.
     * Accepts POST and PUT for convenience (frontend may call PUT).
     */
    @RequestMapping(value = "/update", method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<?> updateLocation(@RequestBody LocationUpdateDto dto) {
        try {
            if (dto == null || dto.getId() == null) {
                log.warn("Location update request missing id or body");
                return ResponseEntity.badRequest().body("Missing id in request payload");
            }

            String id = dto.getId().trim();
            String ts = dto.getTimestamp() != null && !dto.getTimestamp().isBlank() ? dto.getTimestamp() : Instant.now().toString();

            // Validate latitude/longitude presence
            if (dto.getLatitude() == null || dto.getLongitude() == null) {
                log.warn("Location update missing coordinates for id={}", id);
                return ResponseEntity.badRequest().body("Missing latitude or longitude");
            }

            double lat = dto.getLatitude();
            double lon = dto.getLongitude();

            // Validate ranges
            if (lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) {
                log.warn("Invalid coordinates provided for id={} -> lat={}, lon={}", id, lat, lon);
                return ResponseEntity.badRequest().body("Invalid latitude or longitude range");
            }

            // Try User first
            Optional<User> uOpt = userRepository.findById(id);
            if (uOpt.isPresent()) {
                User u = uOpt.get();
                u.setLatitude(lat);
                u.setLongitude(lon);
                u.setLastLocationUpdatedAt(ts);
                userRepository.save(u);

                log.info("Updated location for USER {} => lat={}, lon={}", id, lat, lon);

                // Optionally notify presence updates
                try {
                    chatNotificationService.updateOnlineStatus(id, "ONLINE");
                } catch (Exception ex) {
                    log.debug("Failed to update online status for user {}: {}", id, ex.getMessage());
                }

                return ResponseEntity.ok(u);
            }

            // Try Vendor
            Optional<Vendor> vOpt = vendorRepository.findById(id);
            if (vOpt.isPresent()) {
                Vendor v = vOpt.get();
                v.setLatitude(lat);
                v.setLongitude(lon);
                v.setLastLocationUpdatedAt(ts);
                vendorRepository.save(v);

                log.info("Updated location for VENDOR {} => lat={}, lon={}", id, lat, lon);

                // Optionally notify presence updates
                try {
                    chatNotificationService.updateOnlineStatus(id, "ONLINE");
                } catch (Exception ex) {
                    log.debug("Failed to update online status for vendor {}: {}", id, ex.getMessage());
                }

                return ResponseEntity.ok(v);
            }

            log.warn("No User or Vendor found for id: {}", id);
            return ResponseEntity.status(404).body("No User or Vendor found for id: " + id);
        } catch (Exception e) {
            log.error("Error updating location: {}", e.getMessage(), e);
            // Return a generic message but include a short id reference for correlation
            return ResponseEntity.status(500).body("Error updating location for request");
        }
    }
}
