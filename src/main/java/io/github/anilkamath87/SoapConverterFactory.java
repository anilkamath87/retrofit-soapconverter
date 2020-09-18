package io.github.anilkamath87;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public final class SoapConverterFactory extends Converter.Factory {
    static final MediaType XML = MediaType.get("application/xml; charset=utf-8");


    /**
     * If null, a new JAXB context will be created for each type to be converted.
     */
    private final @Nullable
    JAXBContext context;

    private final @Nullable
    String userName;

    private final @Nullable
    String password;

    private SoapConverterFactory(@Nullable JAXBContext context,
                                 @Nullable String userName,
                                 @Nullable String password) {
        this.context = context;
        this.userName = userName;
        this.password = password;
    }

    public static SoapConverterFactory create() {
        return new SoapConverterFactory(null, null, null);
    }

    public static SoapConverterFactory create(JAXBContext context) {
        if (context == null) {
            throw new NullPointerException("context == null");
        } else {
            return new SoapConverterFactory(context, null, null);
        }
    }

    public static SoapConverterFactory create(JAXBContext context, String userName, String password) {
        if (context == null) {
            throw new NullPointerException("context == null");
        }
        if (isStringBlank(userName) || isStringBlank(password)) {
            throw new RuntimeException("Username or Password == null || \"\"");
        }
        return new SoapConverterFactory(context, userName, password);
    }

    public static SoapConverterFactory create(String userName, String password) {
        if (isStringBlank(userName) || isStringBlank(password)) {
            throw new RuntimeException("Username or Password == null || \"\"");
        }
        return new SoapConverterFactory(null, userName, password);
    }

    private static boolean isStringBlank(final String str) {
        return str == null || str.isEmpty();
    }

    @Nullable
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return type instanceof Class && ((Class) type).isAnnotationPresent(XmlRootElement.class) ? new SoapResponseConverter(this.contextForType((Class) type), (Class) type) : null;
    }

    @Nullable
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return type instanceof Class && ((Class) type).isAnnotationPresent(XmlRootElement.class) ? new SoapRequestConverter<>(this.contextForType((Class) type), (Class) type, userName, password) : null;
    }

    private JAXBContext contextForType(Class<?> type) {
        try {
            return this.context != null ? this.context : JAXBContext.newInstance(type);
        } catch (JAXBException var3) {
            throw new IllegalArgumentException(var3);
        }
    }
}