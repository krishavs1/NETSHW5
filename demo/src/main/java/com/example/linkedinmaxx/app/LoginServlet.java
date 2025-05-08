// src/main/java/com/example/linkedinmaxx/app/LoginServlet.java
package com.example.linkedinmaxx.app;

import com.example.linkedinmaxx.app.dao.User;
import com.example.linkedinmaxx.app.dao.UserDao;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {
  private final UserDao userDao = new UserDao();
  private final Gson    gson    = new Gson();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // read json body
    Map<String, String> body = gson.fromJson(
      new BufferedReader(req.getReader()), Map.class
    );
    String username = body.get("username");
    if (username == null || username.isBlank()) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Username required");
      return;
    }

    try {
      Optional<User> maybe = userDao.findByUsername(username);
      if (maybe.isEmpty()) {
        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unknown user");
        return;
      }
      User u = maybe.get();

      // Set session
      HttpSession session = req.getSession(true);
      session.setAttribute("userId",   u.getId());
      session.setAttribute("username", u.getUsername());

      resp.setContentType("application/json");
      resp.getWriter().write(gson.toJson(Map.of(
        "id", u.getId(),
        "username", u.getUsername()
      )));
    } catch (SQLException e) {
      log("DB error in login", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
    }
  }
}
