
package com.teckit.festival.util;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.web.client.RestClient;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;

public class XmlApiUtil {

    public static <T> T fetchAndParseXml(RestClient client, String uri, Class<T> clazz) {
        byte[] rawXml = client.get()
                .uri(uri)
                .retrieve()
                .body(byte[].class);

        String xml = new String(rawXml, StandardCharsets.UTF_8);

        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return clazz.cast(unmarshaller.unmarshal(new StringReader(xml)));
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new RuntimeException("XML 파싱 오류", e);
        }
    }
}
