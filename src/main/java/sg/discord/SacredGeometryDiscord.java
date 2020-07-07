package sg.discord;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import sg.common.SacredGeometry;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

public class SacredGeometryDiscord {
  private static final String sgCommandExpression = "!sg (.*)";
  private static final Pattern sgCommandPattern = Pattern.compile(sgCommandExpression);

  private static final String token;

  static {
    InputStream inputStream = SacredGeometryDiscord.class.getResourceAsStream(
        "/discord/botToken.txt");
    try {
      token = CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
    } catch (IOException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public static void main(String[] args) {
    DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

    api.addMessageCreateListener(
        event -> {
          Matcher sgCommandMatcher = sgCommandPattern.matcher(event.getMessageContent());
          if (!sgCommandMatcher.matches()) {
            return;
          }
          StringBuilder message = new StringBuilder();

          List<String> commandArguments =
              Splitter.on(' ')
                  .omitEmptyStrings()
                  .trimResults()
                  .splitToList(sgCommandMatcher.group(1));

          try {
            SacredGeometry.sacredGeometry(commandArguments, message::append);
          } catch (IllegalArgumentException e) {
            message.append(e.getMessage());
          }

          event.getChannel().sendMessage(message.toString());
        });

    System.out.println(
        "You can invite SacredGeoBot by using the following url: " + api.createBotInvite());
  }
}
