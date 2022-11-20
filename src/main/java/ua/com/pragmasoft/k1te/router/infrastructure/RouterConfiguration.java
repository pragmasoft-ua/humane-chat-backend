package ua.com.pragmasoft.k1te.router.infrastructure;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import ua.com.pragmasoft.k1te.router.ChatRepository;
import ua.com.pragmasoft.k1te.router.ConversationRepository;
import ua.com.pragmasoft.k1te.router.IdGenerator;
import ua.com.pragmasoft.k1te.router.Router;
import ua.com.pragmasoft.k1te.tg.LastConversations;

public class RouterConfiguration {

  @Produces
  @ApplicationScoped
  public ChatRepository chatRepository() {
    return new InMemoryChatRepository();
  }

  @Produces
  @ApplicationScoped
  public ConversationRepository conversationRepository(IdGenerator idGenerator) {
    return new InMemoryConversationRepository(idGenerator);
  }

  @Produces
  @ApplicationScoped
  public Router router(ChatRepository chatRepository, ConversationRepository conversationRepository,
      LastConversations lastConversations) {
    return new Router(chatRepository, conversationRepository);
  }

  @Produces
  @ApplicationScoped
  public IdGenerator idGenerator() {
    return new NanoIdGenerator();
  }

}
