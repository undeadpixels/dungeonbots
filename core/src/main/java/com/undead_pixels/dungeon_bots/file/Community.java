package com.undead_pixels.dungeon_bots.file;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXLoginPane;
import org.jdesktop.swingx.auth.LoginService;

import com.undead_pixels.dungeon_bots.scene.level.LevelPack;

public class Community {

	public static abstract class TokenRunnable implements Runnable {

		private String token;


		protected String getToken() {
			return token;
		}


		void setToken(String token) {
			this.token = token;
		}
	}


	public static final String API_TOKEN_URL = "https://dungeonbots.herokuapp.com/api/v1/sessions";
	public static final String PACK_UPLOAD_URL = "https://dungeonbots.herokuapp.com/api/v1/packs";


	public static final void login(Runnable onSuccess, TokenRunnable runnable) {

		final JXLoginPane panel = new JXLoginPane(new LoginService() {
			// Based on
			// https://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
			// 4/17/18

			public boolean authenticate(String name, char[] password, String server) {
				URL obj;
				HttpsURLConnection conn = null;
				DataOutputStream writer = null;
				BufferedReader reader = null;
				int responseCode = 0;
				String rawResponse = null;
				try {
					obj = new URL(API_TOKEN_URL);
					conn = (HttpsURLConnection) obj.openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Content-Type", "application/json");
					conn.setRequestProperty("Accept", "application/json");
					String body = "{\"email\":\"" + name + "\",\"password\":\"" + new String(password) + "\"}";
					conn.setDoOutput(true);
					writer = new DataOutputStream(conn.getOutputStream());
					writer.writeBytes(body);
					writer.flush();
					writer.close();
					if ((responseCode = conn.getResponseCode()) != 200) {
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								JOptionPane.showMessageDialog(null, "Bad login.");
							}

						});
						return false;
					}
					reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					String line;
					StringBuffer response = new StringBuffer();
					while ((line = reader.readLine()) != null) {
						response.append(line);
					}
					reader.close();
					rawResponse = response.toString();
				} catch (IOException e) {
					e.printStackTrace();
					final String msg = "Error (" + responseCode + ") while connecting to community.";
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							JOptionPane.showMessageDialog(null, msg);
						}

					});
					return false;
				} finally {
					try {
						writer.close();
					} catch (IOException e) {
						// e.printStackTrace();
					}
					try {
						reader.close();
					} catch (IOException e) {
						// e.printStackTrace();
					}
				}


				// TODO: obliterate password and body and anything else that
				// might contain data.


				runnable.setToken(rawResponse);
				SwingUtilities.invokeLater(runnable);
				return true;
			}
		});


		final JFrame frame = JXLoginPane.showLoginFrame(panel);
		frame.setVisible(true);

	}


	public static final boolean upload(LevelPack pack, String response) {

		@SuppressWarnings("unchecked")
		HashMap<String, String> deserialized = (HashMap<String, String>) Serializer.deserializeFromJSON(response,
				HashMap.class);
		String token = deserialized.get("api_token");

		System.out.println(token);
		if (token == null) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, "Failed to download a token from the community.");
				}

			});
			return false;
		}
		token = response.replaceAll("\"", "");


		int responseCode = 0;
		BufferedReader reader = null;

		URL obj;
		try {
			obj = new URL(PACK_UPLOAD_URL);
			HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", response);

			String body = pack.toJson();
			conn.setDoOutput(true);
			DataOutputStream writer = new DataOutputStream(conn.getOutputStream());
			writer.writeBytes(body);
			writer.flush();
			writer.close();

			responseCode = conn.getResponseCode();
			if ((responseCode = conn.getResponseCode()) != 200) {
				final String msg = "Upload failure (" + responseCode + ")";
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						JOptionPane.showMessageDialog(null, msg);
					}

				});
				return false;
			}
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			StringBuffer sb = new StringBuffer();
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
			String rawResponse = response.toString();
			System.err.println(rawResponse);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	}
}
