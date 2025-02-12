package com.group16.app; 

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
 
public class CompilerTest {

    @Test
    public void CompilerClonesAndCompilesSuccessfully() throws IOException{
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        // Prepare a StringWriter and PrintWriter to capture the output from response.getWriter().
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
        assertTrue(Compiler.compileProj(response, "https://github.com/DD2480-group16-VT25/continuous-integration.git", "main"));
    }

    @Test
    public void CompilerReturnsFalseWithBad() throws IOException{
        HttpServletResponse response = mock(HttpServletResponse.class);
        assertFalse(Compiler.compileProj(response, "", ""));
    }
}
