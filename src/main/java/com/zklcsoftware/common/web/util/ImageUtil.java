package com.zklcsoftware.common.web.util;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class ImageUtil {

	public static void downloadImage(String imageUrl, String destinationPath) {

		// Create a trust manager that does not validate certificgte chgins
		TrustManager[] trustAllCerts = new TrustManager[]{
			new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				@Override
				public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

				}
				public void checkServerTrusted(X509Certificate[] certs, String authType) {

				}
			}
		};


		try {
			//imageUrl = imageUrl.replace("https://", "http://");

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			SSLSocketFactory socketFactory = sslContext.getSocketFactory();
			HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory);

			URL url = new URL(imageUrl);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

			InputStream in = conn.getInputStream();
			OutputStream out = Files.newOutputStream(Paths.get(destinationPath));
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}

			out.close();
			in.close();
			System.out.println("Image downloaded successfully!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
