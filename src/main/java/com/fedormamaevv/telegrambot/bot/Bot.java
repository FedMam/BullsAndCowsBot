package com.fedormamaevv.telegrambot.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Random;

@Component
public class Bot extends TelegramLongPollingBot {
    @Value("${bot.name}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    private String num;
    private String guess;
    private boolean game = false;

    private final int digits = 4;
    private final String[] win_messages = new String[] {"Ух ты, не могу поверить, вы отгадали!",
            "Поздравляю, вы угадали число!",
            "Вы сделали это! УРААААААА!",
            "Нет, пожалуй, всё-таки люди умнее ботов.",
            "А? Я проиграл?" };

    @Override
    public void onUpdateReceived(Update update) {
        final Message message = update.getMessage();
        String msg = message.getText();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        sendMessage.setText("БИП -- ОШИБКА");
        Random rand = new Random();
        try {
            if (msg.compareTo("/start") == 0) {
                sendMessage.setText("Добро пожаловать в игру \"Быки и коровы\"!\n" +
                        "© 2022 Фёдор Мамаев");
                execute(sendMessage);
                sendMessage.setText("Я загадал " + digits + "-значное число. Можете ли вы его угадать?");
                execute(sendMessage);
                num = String.valueOf(rand.nextInt(10000));
                while (num.length() < digits)
                    num = "0" + num;
                guess = "----";
                game = true;
                sendMessage.setText("Ваше число: ");
                execute(sendMessage);
                return;
            }
            else if (game)
            {
                if (msg.length() != digits)
                {
                    sendMessage.setText("Эй, мне нужно " + digits + "-значное число!");
                    execute(sendMessage);
                    return;
                }
                guess = String.valueOf(msg);

                int[] bullscows = check(num, guess);
                sendMessage.setText("Быков: " + bullscows[0] + ", коров: " + bullscows[1]);
                execute(sendMessage);

                if (bullscows[0] == 4)
                {
                    sendMessage.setText(win_messages[rand.nextInt(win_messages.length)]);
                    execute(sendMessage);
                    sendMessage.setText("Вы можете начать новую игру (/start).");
                    execute(sendMessage);
                    game = false;
                }
            }
            else
            {
                sendMessage.setText("Вы можете начать новую игру (/start).");
                execute(sendMessage);
            }
        }
        catch (NumberFormatException nfe)
        {
            sendMessage.setText("Эй, мне нужно " + digits + "-значное число!");
            try { execute(sendMessage); }
            catch (Exception e) { System.out.println(e.getMessage()); }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    private int[] check(String num, String guess)
    {
        int bulls = 0, cows = 0;
        for (int i = 0; i < digits; i++)
        {
            if (num.charAt(i) == guess.charAt(i))
                bulls++;
            else if (num.contains(String.valueOf(guess.charAt(i))))
                cows++;
        }
        return new int[] { bulls, cows };
    }

    public String getBotUsername() { return botUsername; }
    public String getBotToken() { return botToken; }
}
