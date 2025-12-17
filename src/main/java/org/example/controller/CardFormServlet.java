package org.example.controller;
import org.example.controller.util.JsonResponse;
import org.example.controller.util.RequestParser;
import org.example.dto.CardRequestDTO;
import org.example.dto.CardResponseDTO;
import org.example.model.CardTypeEnum;
import org.example.service.CardFormService;
import org.example.service.CardService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@WebServlet(name = "CardFormServlet", urlPatterns = {
    "/api/form/card",
    "/api/form/card/types",