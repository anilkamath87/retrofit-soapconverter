package io.github.anilkamath87;

import okhttp3.ResponseBody;
import retrofit2.Converter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

final class SoapResponseConverter<T> implements Converter<ResponseBody, T> {
    final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    final JAXBContext context;
    final Class<T> type;

    SoapResponseConverter(JAXBContext context, Class<T> type) {
        this.context = context;
        this.type = type;

        // Prevent XML External Entity attacks (XXE).
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

    @Override
    public T convert(ResponseBody value) {
        try {
            Unmarshaller unmarshaller = context.createUnmarshaller();
            XMLStreamReader streamReader = xmlInputFactory.createXMLStreamReader(value.charStream());
            iterateToResponseBody(streamReader);
            return unmarshaller.unmarshal(streamReader, type).getValue();
        } catch (JAXBException | XMLStreamException e) {
            throw new RuntimeException(e);
        } finally {
            value.close();
        }
    }

    private void iterateToResponseBody(final XMLStreamReader streamReader) throws XMLStreamException {
        streamReader.nextTag();
        while (!streamReader.getLocalName().equals("Body")) {
            streamReader.nextTag();
        }
        streamReader.nextTag(); //Iterates to the content of body. eg. MySoapResponseType
    }
}

