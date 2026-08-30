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

    private static final String SYSTEM_PROMPT = """
            Eres el asistente virtual de una clínica médica. Tu trabajo es ayudar a los pacientes
            a agendar citas médicas de forma conversacional y amigable.
            
            Cuando el paciente quiera agendar una cita, necesitas recopilar:
            - Qué especialidad o doctor necesita
            - Su id de paciente (si no lo sabe, pídele que confirme que ya está registrado)
            - Fecha y hora deseada
            
            Usa las herramientas disponibles para consultar especialidades, doctores y horarios
            reales antes de confirmar una cita. Nunca inventes información sobre doctores,
            especialidades u horarios: siempre consulta primero.
            
            Si la fecha u hora que pide el paciente no está disponible, sugiere consultar
            el horario del doctor y ofrece alternativas dentro de ese horario.
            
            Recuerda el contexto de la conversación: si el paciente ya mencionó un doctor,
            una especialidad o su id de paciente antes, no vuelvas a pedirlo.
            
            Responde siempre en español, de forma breve y clara.
            """;

    @PostMapping
    public String chatear(@RequestBody ChatRequest request) {
        return chatClientBuilder.build()
                .prompt()
                .system(SYSTEM_PROMPT)
                .user(request.mensaje())
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, request.conversationId()))
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .tools(citasTools)
                .call()
                .content();
    }

    public record ChatRequest(String mensaje, String conversationId) {
    }
}