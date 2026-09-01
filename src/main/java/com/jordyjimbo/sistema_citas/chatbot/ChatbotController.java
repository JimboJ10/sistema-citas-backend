package com.jordyjimbo.sistema_citas.chatbot;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatClient.Builder chatClientBuilder;
    private final CitasTools citasTools;
    private final ChatMemory chatMemory;

    private static final String SYSTEM_PROMPT_BASE = """
            Eres el asistente virtual de una clínica médica. Tu trabajo es ayudar a los pacientes
            a agendar citas médicas de forma conversacional y amigable.
            
            Cuando el paciente quiera agendar una cita, necesitas saber:
            - Qué especialidad o doctor necesita
            - Quién es el paciente (ver instrucciones de identificación abajo)
            - Fecha y hora deseada
            
            IDENTIFICACIÓN DEL PACIENTE - muy importante:
            Nunca le pidas al paciente un "id" o "id de paciente" directamente, esa es información
            interna que las personas no conocen ni deberían tener que dar. En vez de eso:
            %s
            
            Usa las herramientas disponibles para consultar especialidades, doctores y horarios
            reales antes de confirmar una cita. Nunca inventes información sobre doctores,
            especialidades u horarios: siempre consulta primero.
            
            Si la fecha u hora que pide el paciente no está disponible, sugiere consultar
            el horario del doctor y ofrece alternativas dentro de ese horario.
            
            Recuerda el contexto de la conversación: si el paciente ya mencionó un doctor
            o una especialidad antes, no vuelvas a pedirlo.
            
            Responde siempre en español, de forma breve y clara.
            """;

    private static final String INSTRUCCION_PACIENTE_IDENTIFICADO = """
            Este usuario ya inició sesión y su id de paciente es %d. Úsalo directamente en
            la herramienta de crear cita sin mencionarlo ni pedírselo en la conversación.
            """;

    private static final String INSTRUCCION_PACIENTE_INVITADO = """
            Este usuario NO tiene cuenta. Cuando quiera agendar una cita, pídele su nombre
            completo y su número de teléfono (de forma natural, en la conversación), y usa
            la herramienta registrarPacienteInvitado con esos datos para obtener su id antes
            de crear la cita.
            """;

    @PostMapping
    public String chatear(@RequestBody ChatRequest request) {
        String instruccionIdentificacion = request.pacienteId() != null
                ? INSTRUCCION_PACIENTE_IDENTIFICADO.formatted(request.pacienteId())
                : INSTRUCCION_PACIENTE_INVITADO;

        String systemPrompt = SYSTEM_PROMPT_BASE.formatted(instruccionIdentificacion);

        return chatClientBuilder.build()
                .prompt()
                .system(systemPrompt)
                .user(request.mensaje())
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, request.conversationId()))
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .tools(citasTools)
                .call()
                .content();
    }

    public record ChatRequest(String mensaje, String conversationId, Long pacienteId) {
    }
}