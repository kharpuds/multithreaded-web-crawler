package com.eulerity.hackathon.imagefinder;


import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;

public class ImageFinderTest {

    public HttpServletRequest request;
    public HttpServletResponse response;
    public StringWriter sw;
    public HttpSession session;

    private static final String INVALID_URL = "invalid_url";
    private static final String TEST_URL = "https://www.google.com/";

    @Before
    public void setUp() throws Exception {
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Mockito.when(response.getWriter()).thenReturn(pw);
        Mockito.when(request.getRequestURI()).thenReturn("/foo/foo/foo");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/foo/foo/foo"));
        session = Mockito.mock(HttpSession.class);
        Mockito.when(request.getSession()).thenReturn(session);
    }

    @Test
    public void test() throws IOException {
        Mockito.when(request.getServletPath()).thenReturn("/main");
        Mockito.when(request.getParameter("url")).thenReturn(TEST_URL);
        new ImageFinder().doPost(request, response);
        Assert.assertNotNull(new Gson().toJson(ImageFinder.resultMap));
    }

    @Test(expected = MalformedURLException.class)
    public void testInvalidURL() throws IOException {
        Mockito.when(request.getServletPath()).thenReturn("/main");
        Mockito.when(request.getParameter("url")).thenReturn(INVALID_URL);
        new ImageFinder().doPost(request, response);
        Assert.assertNull(new Gson().toJson(ImageFinder.resultMap));
    }
}



