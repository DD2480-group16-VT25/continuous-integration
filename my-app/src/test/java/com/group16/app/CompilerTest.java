package com.group16.app; 

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
 
public class CompilerTest {

    @Test
    public void CompilerClonesAndCompilesSuccessfully() throws IOException{
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        // Prepare a StringWriter and PrintWriter to capture the output from response.getWriter().
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
        assertTrue(Compiler.compileProj(response, "https://github.com/DD2480-group16-VT25/continuous-integration.git", "compile"));
    }

    @Test
    public void CompilerClonesAndCompilesUnsuccessfullyWithEmptyInput(){
        HttpServletResponse response = mock(HttpServletResponse.class);
        assertThrows(NullPointerException.class, () -> {
            Compiler.compileProj(response, "", "");
        });
    }
}
