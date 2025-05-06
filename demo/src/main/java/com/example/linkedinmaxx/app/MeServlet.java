// src/main/java/com/example/linkedinmaxx/app/MeServlet.java
package com.example.linkedinmaxx.app;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/api/me")
public class MeServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    HttpSession session = req.getSession(false);
    if (session == null || session.getAttribute("username") == null) {
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
      return;
    }

    String username = (String) session.getAttribute("username");
    resp.setContentType("application/json");
    resp.getWriter()
        .write("{\"username\": \"" + username.replace("\"","\\\"") + "\"}");
  }
}
