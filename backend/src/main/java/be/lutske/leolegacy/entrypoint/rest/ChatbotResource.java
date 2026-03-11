package be.lutske.leolegacy.entrypoint.rest;

import be.lutske.leolegacy.usecase.chatbot.AnswerRecipeChatMessageUseCase;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/chatbot/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ChatbotResource {

    private final AnswerRecipeChatMessageUseCase answerRecipeChatMessageUseCase;

    public ChatbotResource(AnswerRecipeChatMessageUseCase answerRecipeChatMessageUseCase) {
        this.answerRecipeChatMessageUseCase = answerRecipeChatMessageUseCase;
    }

    @POST
    public ChatbotDtos.ChatResponse chat(ChatbotDtos.ChatRequest request) {
        var reply = answerRecipeChatMessageUseCase.answer(
                request == null ? null : request.message(),
                request == null || request.context() == null ? java.util.List.of() : request.context().stream()
                        .map(context -> new be.lutske.leolegacy.usecase.chatbot.ChatMessageContext(context.role(), context.message()))
                        .toList()
        );
        return RestChatbotMapper.toResponse(reply);
    }
}
