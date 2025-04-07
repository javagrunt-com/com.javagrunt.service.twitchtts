package com.javagrunt.service.chatbot;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import jakarta.annotation.PostConstruct;
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.InputStream;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}

@Component
class TwitchClientComponent {
	private final TwitchClient twitchClient;
	private MaryInterface marytts;

	public TwitchClientComponent() {
		this.twitchClient = TwitchClientBuilder.builder()
//				.withEnableHelix(true)
				.withEnableChat(true)
				.build();
	}

	@PostConstruct
	void init()	{
		twitchClient.getChat().joinChannel("javagrunt");
		try {
			marytts = new LocalMaryInterface();
		}catch(MaryConfigurationException mce){
			mce.printStackTrace();
		}

		twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, event -> {
			String user = event.getUser().getName();
			String message = event.getMessage();
			String toSpeak = user + " says: " + message;
			System.out.println(toSpeak);
			speak(toSpeak);
		});
	}

	private void speak(String text) {
		try {
			AudioInputStream audioStream = marytts.generateAudio(text); // Already good to go
			Clip clip = AudioSystem.getClip();
			clip.open(audioStream);
			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
