package eu.arrowhead.core.authorization;

import java.io.File;
import java.io.FileInputStream;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.ArrowheadToken;
import eu.arrowhead.common.model.messages.RawTokenInfo;

public class AuthorizationService {
	private static Properties prop;

	public static List<PublicKey> getProviderPublicKeys(List<ArrowheadSystem> providers) {
		try {
			DatabaseManager databaseManager = DatabaseManager.getInstance();

			HashMap<String, Object> restrictionMap = new HashMap<String, Object>();

			List<PublicKey> keys = new ArrayList<PublicKey>();

			for (ArrowheadSystem provider : providers) {
				restrictionMap.clear();
				restrictionMap.put("systemGroup", provider.getSystemGroup());
				restrictionMap.put("systemName", provider.getSystemName());
				ArrowheadSystem foundProvider = databaseManager.get(ArrowheadSystem.class, restrictionMap);

				if (foundProvider == null) {
					throw new DataNotFoundException("Consumer System is not in the authorization database.");
				}
				String authenticationInfo = foundProvider.getAuthenticationInfo();
				PublicKey key = getPublicKey(authenticationInfo);
				keys.add(key);
			}

			return keys;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	public static ArrowheadToken generateSingleToken(ArrowheadSystem provider, PublicKey providerKey, ArrowheadSystem consumer,
			ArrowheadCloud consumerCloud, ArrowheadService service, int duration) throws Exception {
		
		RawTokenInfo rawTokenInfo = new RawTokenInfo();

		String c = consumer.getSystemName() + "." + consumer.getSystemGroup();
		if (consumerCloud.getCloudName() != null) {
			c = c.concat(".").concat(consumerCloud.getCloudName()).concat(".").concat(consumerCloud.getOperator());
		} else {
			ArrowheadCloud ownCloud = Utility.getOwnCloud();
			c = c.concat(".").concat(ownCloud.getCloudName()).concat(".").concat(ownCloud.getOperator());
		}
		rawTokenInfo.setC(c);
		
		//TODO ?????
		String s = service.getInterfaces() + "." + service.getServiceDefinition() + "." + service.getServiceGroup(); 
		rawTokenInfo.setS(s);
		
		String i = Long.toString(System.currentTimeMillis());
		rawTokenInfo.setI(i);
		
		String d = Integer.toString(duration);
		rawTokenInfo.setD(d);

		Gson gson = new Gson();
		String json = gson.toJson(rawTokenInfo);
		if (json.length() > 200) {
			throw new Exception("JSON is too large");
		}

		byte[] jsonbytes = json.getBytes("UTF8");

		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, providerKey);
			byte[] token = cipher.doFinal(jsonbytes);

			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initSign(AuthorizationMain.privateKey);
			signature.update(token);
			String tokenString = Base64.getEncoder().encodeToString(token);
			byte[] sigBytes = signature.sign();
			String sign = Base64.getEncoder().encodeToString(sigBytes);

			ArrowheadToken arrowheadToken = new ArrowheadToken();
			arrowheadToken.setToken(tokenString);
			arrowheadToken.setSignature(sign);

			return arrowheadToken;
		} catch (Exception ex) {
			Response.status(500).build();
			ex.printStackTrace();
			return null;
		}
	}

	public synchronized static Properties getProp() {
		try {
			if (prop == null) {
				prop = new Properties();
				File file = new File("config" + File.separator + "app.properties");
				FileInputStream inputStream = new FileInputStream(file);
				if (inputStream != null) {
					prop.load(inputStream);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return prop;
	}

	public static PublicKey getPublicKey(String stringKey) {
		try {
			byte[] byteKey = Base64.getDecoder().decode(stringKey);
			X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
			KeyFactory kf = KeyFactory.getInstance("RSA");

			return kf.generatePublic(X509publicKey);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}