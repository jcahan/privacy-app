package com.example.columbiaprivacyapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebParse {

	public static void main(String[] args) throws IOException {
		String line = null;
		URL theURL = new URL("http://quiet-badlands-8312.herokuapp.com/keywords?lat=40.8095761&lon=-72.9608287");
		HttpURLConnection conn = (HttpURLConnection) theURL.openConnection();
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		System.out.println(rd.readLine());
		while (rd.readLine() != null) {
			rd.readLine();
			line += rd.readLine();
		}
		System.out.println(line);
	}

}
