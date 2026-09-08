package com.frauddetection.payment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PaymentServiceApplicationTests {

	@Test
	void contextLoads() {
		// Integration test - runs with test profile
		// Full context test will run once Docker Compose is set up
	}
}