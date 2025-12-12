package com.tcon.webid.service;

import org.springframework.stereotype.Service;

/**
 * Service for generating modern HTML email templates
 */
@Service
public class EmailTemplateService {

    private static final String EMAIL_HEADER = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7fa; }
                .email-container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }
                .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 40px 20px; text-align: center; }
                .header h1 { color: #ffffff; margin: 0; font-size: 28px; font-weight: 600; }
                .header p { color: #f0f0f0; margin: 10px 0 0 0; font-size: 14px; }
                .content { padding: 40px 30px; }
                .content h2 { color: #333333; font-size: 24px; margin: 0 0 20px 0; }
                .content p { color: #555555; font-size: 16px; line-height: 1.6; margin: 10px 0; }
                .info-box { background-color: #f8f9fa; border-left: 4px solid #667eea; padding: 20px; margin: 20px 0; border-radius: 4px; }
                .info-box strong { color: #333333; display: inline-block; min-width: 150px; }
                .otp-code { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #ffffff; font-size: 32px; font-weight: bold; text-align: center; padding: 20px; margin: 20px 0; border-radius: 8px; letter-spacing: 8px; }
                .button { display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #ffffff; text-decoration: none; padding: 14px 40px; border-radius: 6px; font-weight: 600; margin: 20px 0; }
                .footer { background-color: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e0e0e0; }
                .footer p { color: #888888; font-size: 14px; margin: 5px 0; }
                .divider { height: 1px; background: linear-gradient(to right, transparent, #e0e0e0, transparent); margin: 30px 0; }
                .warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; border-radius: 4px; color: #856404; }
                .success { background-color: #d4edda; border-left: 4px solid #28a745; padding: 15px; margin: 20px 0; border-radius: 4px; color: #155724; }
            </style>
        </head>
        <body>
            <div class="email-container">
        """;

    private static final String EMAIL_FOOTER = """
                <div class="footer">
                    <p><strong>Event Bidding Platform</strong></p>
                    <p>Making your events memorable with the best vendors</p>
                    <p style="margin-top: 20px;">
                        <a href="#" style="color: #667eea; text-decoration: none; margin: 0 10px;">Website</a> |
                        <a href="#" style="color: #667eea; text-decoration: none; margin: 0 10px;">Support</a> |
                        <a href="#" style="color: #667eea; text-decoration: none; margin: 0 10px;">Contact</a>
                    </p>
                    <p style="color: #aaaaaa; font-size: 12px; margin-top: 20px;">
                        &copy; 2025 Event Bidding. All rights reserved.
                    </p>
                </div>
            </div>
        </body>
        </html>
        """;

    /**
     * Generate OTP email template
     */
    public String generateOtpEmail(String otp) {
        return EMAIL_HEADER + """
            <div class="header">
                <h1>🔐 Verification Code</h1>
                <p>Your One-Time Password</p>
            </div>
            <div class="content">
                <h2>Hello!</h2>
                <p>You requested a verification code to access your account. Please use the code below:</p>
                
                <div class="otp-code">%s</div>
                
                <div class="warning">
                    <strong>⏰ Important:</strong> This code will expire in <strong>5 minutes</strong>.
                </div>
                
                <p>If you didn't request this code, please ignore this email or contact support if you have concerns.</p>
                
                <div class="divider"></div>
                
                <p style="color: #888888; font-size: 14px;">
                    <strong>Security Tip:</strong> Never share your OTP with anyone. Our team will never ask for your verification code.
                </p>
            </div>
            """.formatted(otp) + EMAIL_FOOTER;
    }

    /**
     * Generate vendor registration email template
     */
    public String generateVendorRegistrationEmail(String contactName, String vendorOrgId,
                                                   String businessName, String email, String mobile) {
        return EMAIL_HEADER + """
            <div class="header">
                <h1>🎉 Welcome to Event Bidding!</h1>
                <p>Your vendor registration is complete</p>
            </div>
            <div class="content">
                <h2>Congratulations, %s!</h2>
                <p>Your vendor account has been successfully created. You're now ready to start receiving event requests and submitting bids.</p>
                
                <div class="success">
                    <strong>✓ Registration Successful</strong><br>
                    Your account is now active and ready to use!
                </div>
                
                <div class="info-box">
                    <p><strong>Vendor Organization ID:</strong> <span style="color: #667eea; font-size: 18px; font-weight: 600;">%s</span></p>
                    <p style="font-size: 14px; color: #888888; margin-top: 10px;">
                        ⚠️ Please save this ID - you'll need it to login to the system
                    </p>
                </div>
                
                <div class="divider"></div>
                
                <h3 style="color: #333333; font-size: 18px;">Your Account Details:</h3>
                <div class="info-box">
                    <p><strong>Business Name:</strong> %s</p>
                    <p><strong>Contact Person:</strong> %s</p>
                    <p><strong>Email:</strong> %s</p>
                    <p><strong>Mobile:</strong> %s</p>
                </div>
                
                <div style="text-align: center; margin: 30px 0;">
                    <a href="#" class="button">Complete Your Profile →</a>
                </div>
                
                <h3 style="color: #333333; font-size: 18px;">What's Next?</h3>
                <p>✓ Complete your service details and specialties</p>
                <p>✓ Upload your portfolio and certifications</p>
                <p>✓ Start receiving event requests</p>
                <p>✓ Submit competitive bids and grow your business</p>
                
                <div class="divider"></div>
                
                <p style="color: #888888; font-size: 14px;">
                    Need help? Our support team is here to assist you 24/7.
                </p>
            </div>
            """.formatted(contactName, vendorOrgId, businessName, contactName, email, mobile) + EMAIL_FOOTER;
    }

    /**
     * Generate username retrieval email template
     */
    public String generateUsernameEmail(String username, boolean isVendor, String businessName) {
        String title = isVendor ? "Vendor Organization ID" : "Username";
        String userType = isVendor ? "Vendor" : "User";

        return EMAIL_HEADER + """
            <div class="header">
                <h1>🔑 %s Recovery</h1>
                <p>Your account information</p>
            </div>
            <div class="content">
                <h2>Hello!</h2>
                <p>You requested your %s information. Here are your account details:</p>
                
                <div class="info-box">
                    <p><strong>Your %s:</strong></p>
                    <p style="color: #667eea; font-size: 20px; font-weight: 600; margin-top: 10px;">%s</p>
                    %s
                </div>
                
                <div style="text-align: center; margin: 30px 0;">
                    <a href="#" class="button">Login to Your Account →</a>
                </div>
                
                <div class="warning">
                    <strong>Security Notice:</strong> If you didn't request this information, please contact our support team immediately.
                </div>
                
                <div class="divider"></div>
                
                <p style="color: #888888; font-size: 14px;">
                    <strong>Forgot Password?</strong> You can reset it from the login page.
                </p>
            </div>
            """.formatted(
                title,
                title.toLowerCase(),
                title,
                username,
                isVendor ? "<p><strong>Business Name:</strong> " + businessName + "</p>" : ""
            ) + EMAIL_FOOTER;
    }

    /**
     * Generate password reset confirmation email
     */
    public String generatePasswordResetEmail(String name) {
        return EMAIL_HEADER + """
            <div class="header">
                <h1>✅ Password Reset Successful</h1>
                <p>Your password has been updated</p>
            </div>
            <div class="content">
                <h2>Hello %s!</h2>
                
                <div class="success">
                    <strong>✓ Success!</strong><br>
                    Your password has been successfully reset.
                </div>
                
                <p>You can now login to your account using your new password.</p>
                
                <div style="text-align: center; margin: 30px 0;">
                    <a href="#" class="button">Login Now →</a>
                </div>
                
                <div class="warning">
                    <strong>⚠️ Didn't reset your password?</strong><br>
                    If you didn't perform this action, please contact our support team immediately to secure your account.
                </div>
                
                <div class="divider"></div>
                
                <h3 style="color: #333333; font-size: 18px;">Security Tips:</h3>
                <p>• Use a strong, unique password</p>
                <p>• Never share your password with anyone</p>
                <p>• Enable two-factor authentication</p>
                <p>• Change your password regularly</p>
            </div>
            """.formatted(name) + EMAIL_FOOTER;
    }

    /**
     * Generate welcome email for users
     */
    public String generateWelcomeEmail(String name, String email) {
        return EMAIL_HEADER + """
            <div class="header">
                <h1>🎊 Welcome to Event Bidding!</h1>
                <p>Start planning your perfect event</p>
            </div>
            <div class="content">
                <h2>Welcome, %s!</h2>
                <p>Thank you for joining Event Bidding! We're excited to help you create unforgettable events.</p>
                
                <div class="success">
                    <strong>✓ Account Created Successfully</strong><br>
                    Your account is ready to use!
                </div>
                
                <div class="info-box">
                    <p><strong>Your Email:</strong> %s</p>
                    <p style="font-size: 14px; color: #888888; margin-top: 10px;">
                        Use this email to login to your account
                    </p>
                </div>
                
                <div style="text-align: center; margin: 30px 0;">
                    <a href="#" class="button">Start Planning Your Event →</a>
                </div>
                
                <h3 style="color: #333333; font-size: 18px;">What You Can Do:</h3>
                <p>✓ Browse verified vendors and their services</p>
                <p>✓ Request quotes from multiple vendors</p>
                <p>✓ Compare bids and select the best offer</p>
                <p>✓ Manage your events in one place</p>
                <p>✓ Rate and review vendors</p>
                
                <div class="divider"></div>
                
                <p style="color: #888888; font-size: 14px;">
                    Questions? Our support team is available 24/7 to help you.
                </p>
            </div>
            """.formatted(name, email) + EMAIL_FOOTER;
    }
}

