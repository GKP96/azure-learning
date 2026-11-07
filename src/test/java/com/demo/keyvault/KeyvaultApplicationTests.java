package com.demo.keyvault;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"azure.storage.connection-string=DefaultEndpointsProtocol=https;AccountName=testaccount;AccountKey=dGVzdA==;EndpointSuffix=core.windows.net"
})
class KeyvaultApplicationTests {

	@Test
	void contextLoads() {
	}

}
