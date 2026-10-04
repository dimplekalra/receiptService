package com.maven.receipts.integrationTests;

import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;

public class TestFixtureLoader {
        private TestFixtureLoader() {
        }

        public static MockMultipartFile receipt(String filename)
                        throws IOException {

                ClassPathResource resource = new ClassPathResource("fixtures/task-a/" + filename);

                InputStream inputStream = resource.getInputStream();

                return new MockMultipartFile(
                                "file",
                                filename,
                                "text/plain",
                                inputStream);
        }
}
