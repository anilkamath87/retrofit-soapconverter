package io.github.anilkamath87;

import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.Converter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.soap.*;
import java.io.IOException;

final class SoapRequestConverter<T> implements Converter<T, RequestBody> {
    final JAXBContext context;
    final Class<T> type;
    final String userName;
    final String password;

    public SoapRequestConverter(JAXBContext context, Class<T> type) {
        this(context, type, null, null);
    }

    public SoapRequestConverter(final JAXBContext contextForType, final Class type, final String userName, final String password) {
        this.context = contextForType;
        this.type = type;
        this.userName = userName;
        this.password = password;
    }

    public RequestBody convert(T value) {
        Buffer buffer = new Buffer();

        try {
            final SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
            final SOAPPart soapPart = soapMessage.getSOAPPart();
            final SOAPEnvelope soapEnvelope = soapPart.getEnvelope();

            addSecurity(soapEnvelope);

            final SOAPBody soapBody = soapMessage.getSOAPBody();
            Marshaller marshaller = this.context.createMarshaller();
            marshaller.marshal(value, soapBody);
            soapMessage.writeTo(buffer.outputStream());
        } catch (JAXBException | SOAPException | IOException e) {
            throw new RuntimeException(e);
        }

        return RequestBody.create(SoapConverterFactory.XML, buffer.readByteString());
    }

    private void addSecurity(final SOAPEnvelope soapEnvelope) throws SOAPException {
        final SOAPHeader soapHeader = soapEnvelope.getHeader();
        Name headerElementName = soapEnvelope.createName(
                "Security",
                "wsse",
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"
        );

        SOAPHeaderElement soapHeaderElement = soapHeader.addHeaderElement(headerElementName);

        SOAPElement usernameTokenSOAPElement = soapHeaderElement.addChildElement("UsernameToken", "wsse");
        SOAPElement userNameSOAPElement = usernameTokenSOAPElement.addChildElement("Username", "wsse");
        userNameSOAPElement.addTextNode(userName);

        SOAPElement passwordSOAPElement = usernameTokenSOAPElement.addChildElement("Password", "wsse");
        SOAPFactory soapFactory = SOAPFactory.newInstance();
        passwordSOAPElement.addAttribute(soapFactory.createName("Type"),
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");

        passwordSOAPElement.addTextNode(password);
    }
}

