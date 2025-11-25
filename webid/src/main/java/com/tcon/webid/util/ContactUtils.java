package com.tcon.webid.util;

import java.util.ArrayList;
import java.util.List;

public class ContactUtils {

    // Normalize email: trim and lowercase
    public static String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }

    // Normalize mobile: remove common separators and ensure a leading +country code.
    // If number has 10 digits, assume India (+91). If it already starts with +, keep it.
    public static String normalizeMobile(String mobile) {
        if (mobile == null) return null;
        String cleaned = mobile.trim().replaceAll("[\\s\\-()]+", "");
        if (cleaned.isEmpty()) return null;
        if (cleaned.startsWith("+")) {
            return cleaned;
        }
        // if starts with country code without + (e.g., 919876543210), add +
        if (cleaned.matches("^91[0-9]{10}$")) {
            return "+" + cleaned;
        }
        // if 10 digits, assume India
        if (cleaned.matches("^[0-9]{10}$")) {
            return "+91" + cleaned;
        }
        // if other valid-looking digits length (11-15), prefix +
        if (cleaned.matches("^[0-9]{11,15}$")) {
            return "+" + cleaned;
        }
        // otherwise return cleaned as-is (best-effort)
        return cleaned;
    }

    // Generate a list of candidate mobile values to search for in DB to support legacy stored formats
    // Returns candidates in preferred order (most-normalized first).
    public static List<String> mobileSearchCandidates(String input) {
        List<String> out = new ArrayList<>();
        if (input == null) return out;
        String raw = input.trim().replaceAll("[\\s\\-()]+", "");
        if (raw.isEmpty()) return out;

        // Normalized (with + and country code where possible)
        String norm = normalizeMobile(raw);
        if (norm != null && !out.contains(norm)) out.add(norm);

        // Add variant without plus (e.g., '919848299232')
        if (norm != null && norm.startsWith("+")) {
            String noPlus = norm.substring(1);
            if (!out.contains(noPlus)) out.add(noPlus);
        }

        // Raw (as-is), e.g., '9848299232' or '+919848299232'
        if (!out.contains(raw)) out.add(raw);

        // If normalized starts with +91, also add the local 10-digit variant (without country code)
        if (norm != null && norm.startsWith("+91")) {
            String local10 = norm.substring(3); // after +91
            if (local10.matches("^[0-9]{10}$") && !out.contains(local10)) out.add(local10);
        }

        // Also if normalized starts with 91 (without +), add the local 10-digit variant
        if (norm != null && norm.startsWith("91") && norm.length() >= 12) {
            String maybeLocal = norm.substring(norm.length() - 10);
            if (maybeLocal.matches("^[0-9]{10}$") && !out.contains(maybeLocal)) out.add(maybeLocal);
        }

        return out;
    }
}
