package com.lvt.apps.myvote.ms.logging;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Getter
@Slf4j
public class LogInterceptor implements ClientInterceptor {

    private String requestString;
    private String responseString;

    /**
     * Empty implementation to comply with ClientInterceptor interface
     * @param messageContext contains the outgoing request message
     * @return boolean hardcoded true
     * @throws WebServiceClientException exception thrown on handler execution, if any
     */
    @Override public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
        return true;
    }

    /**
     * Empty implementation to comply with ClientInterceptor interface
     * @param messageContext contains the outgoing request message
     * @return boolean hardcoded true
     * @throws WebServiceClientException
     */
    @Override public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
        return true;
    }

    /**
     * Get the request in String format
     * @param messageContext contains the outgoing request message
     * @return boolean hardcoded true
     * @throws WebServiceClientException
     */
    @Override
    public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
        try {
            SaajSoapMessage message = (SaajSoapMessage) messageContext.getRequest();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            message.writeTo(stream);
            requestString = stream.toString();
        } catch (IOException e) {
            log.info("Unable to parse and log SOAP request, exception is: {}", e);
        }
        return true;
    }

    /**
     * Get the response in String format
     * @param messageContext contains both request and response messages, the response should contains a Fault
     * @param ex exception thrown on handler execution, if any
     * @throws WebServiceClientException
     */
    @Override
    public void afterCompletion(MessageContext messageContext, Exception ex) throws WebServiceClientException {
        try {
            SaajSoapMessage message = (SaajSoapMessage) messageContext.getResponse();
            OutputStream outputStream = new ByteArrayOutputStream();
            message.writeTo(outputStream);
            responseString = outputStream.toString();
        } catch (IOException e) {
            log.info("Unable to parse and log SOAP response, exception is: {}", e);
        }
    }

}

