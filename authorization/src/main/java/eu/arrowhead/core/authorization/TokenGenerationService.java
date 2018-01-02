package eu.arrowhead.core.authorization;

import com.google.gson.Gson;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.messages.ArrowheadToken;
import eu.arrowhead.common.messages.RawTokenInfo;
import eu.arrowhead.common.messages.TokenGenerationRequest;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import org.apache.log4j.Logger;

class TokenGenerationService {

  private static final Logger log = Logger.getLogger(TokenGenerationService.class.getName());


  static List<ArrowheadToken> generateTokens(TokenGenerationRequest request) {
    // First get the public key for each provider
    List<PublicKey> publicKeys = getProviderPublicKeys(request.getProviders());

    // Then create the ArrowheadToken for each provider (starting with the variable initializations before the for loop)
    RawTokenInfo rawTokenInfo = new RawTokenInfo();
    List<ArrowheadToken> tokens = new ArrayList<>();
    Cipher cipher;
    try {
      cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      log.fatal("Cipher.getInstance(String) throws exception, code needs to be changed!");
      return tokens;
    }
    Signature signature;
    try {
      signature = Signature.getInstance("SHA1withRSA");
      signature.initSign(AuthorizationMain.privateKey);
    } catch (NoSuchAlgorithmException e) {
      log.fatal("Signature.getInstance(String) throws exception, code needs to be changed!");
      return tokens;
    } catch (InvalidKeyException e) {
      log.fatal("The private key of the Authorization module is invalid, keystore needs to be changed!");
      return tokens;
    }

    for (PublicKey key : publicKeys) {
      // Can not generate token without the provider public key
      if (key == null) {
        tokens.add(null);
        continue;
      }

      // Set consumer info string
      String c = request.getConsumer().getSystemName() + "." + request.getConsumer().getSystemGroup();
      if (request.getConsumerCloud() != null) {
        c = c.concat(".").concat(request.getConsumerCloud().getCloudName()).concat(".").concat(request.getConsumerCloud().getOperator());
      } else {
        ArrowheadCloud ownCloud = Utility.getOwnCloud();
        c = c.concat(".").concat(ownCloud.getCloudName()).concat(".").concat(ownCloud.getOperator());
      }
      rawTokenInfo.setC(c);

      // Set service info string
      String s = request.getService().getInterfaces().get(0) + "." + request.getService().getServiceDefinition() + "." + request.getService()
          .getServiceGroup();
      rawTokenInfo.setS(s);

      // Set the token validity duration
      if (request.getDuration() != 0) {
        long endTime = System.currentTimeMillis() + request.getDuration();
        rawTokenInfo.setE(endTime);
      } else {
        // duration = 0 means a token is valid without a time limitation
        rawTokenInfo.setE(0L);
      }

      // There is an upper limit for the size of the token info, skip providers which exceeds this limit
      String json = Utility.gson.toJson(rawTokenInfo);
      //System.out.println("Raw token info: ");
      //System.out.println(json);
      if (json.length() > 244) {
        tokens.add(null);
        log.error("ArrowheadToken exceeded the size limit. Skipped provider.");
        continue;
      }

      // Finally, generate the token and signature strings
      try {
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] tokenBytes = cipher.doFinal(json.getBytes("UTF8"));
        //System.out.println("Token bytes: " + Arrays.toString(tokenBytes));
        signature.update(tokenBytes);
        byte[] sigBytes = signature.sign();
        //System.out.println("Signature bytes: " + Arrays.toString(sigBytes));

        String tokenString = Base64.getEncoder().encodeToString(tokenBytes);
        String signatureString = Base64.getEncoder().encodeToString(sigBytes);
        tokens.add(new ArrowheadToken(tokenString, signatureString));
      } catch (Exception e) {
        e.printStackTrace();
        log.error("Cipher or Signature class throws public key specific exception: " + e.getMessage());
        tokens.add(null);
      }
    }

    // Throw an exception if none of the token generation was successful
    boolean nonNullTokenExists = false;
    for (ArrowheadToken token : tokens) {
      if (token != null) {
        nonNullTokenExists = true;
        break;
      }
    }
    if (!nonNullTokenExists) {
      log.error("None of the provider ArrowheadSystems in this orchestration have a valid RSA public key spec stored in the database.");
      throw new RuntimeException("Token generation failed for all the provider ArrowheadSystems.");
    }

    return tokens;
  }


  private static List<PublicKey> getProviderPublicKeys(List<ArrowheadSystem> providers) {
    HashMap<String, Object> restrictionMap = new HashMap<>();
    List<PublicKey> keys = new ArrayList<>();

    for (ArrowheadSystem provider : providers) {
      // Get the provider from the database
      restrictionMap.clear();
      restrictionMap.put("systemGroup", provider.getSystemGroup());
      restrictionMap.put("systemName", provider.getSystemName());
      ArrowheadSystem retrievedProvider = AuthorizationResource.dm.get(ArrowheadSystem.class, restrictionMap);

      if (retrievedProvider != null) {
        try {
          PublicKey key = getPublicKey(retrievedProvider.getAuthenticationInfo());
          keys.add(key);
        } catch (InvalidKeySpecException | NullPointerException e) {
          log.error("The stored auth info for the ArrowheadSystem " + provider.toString()
                        + " is not a proper RSA public key spec, or it is incorrectly encoded. The public key can not be generated from it.");
          keys.add(null);
        }
      } else {
        // In theory this branch will not get called, since the Orchestrator will filter out systems not in the database.
        keys.add(null);
      }
    }

    // Throw an exception if none of the public kezs could be acquired from the specs
    boolean nonNullKeyExists = false;
    for (PublicKey key : keys) {
      if (key != null) {
        nonNullKeyExists = true;
        break;
      }
    }
    if (!nonNullKeyExists) {
      log.error("None of the provider ArrowheadSystems in this orchestration have a valid RSA public key spec stored in the database.");
      throw new RuntimeException("Token generation failed for all the provider ArrowheadSystems.");
    }

    return keys;
  }

  private static PublicKey getPublicKey(String stringKey) throws InvalidKeySpecException {
    byte[] byteKey = Base64.getDecoder().decode(stringKey);
    X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
    KeyFactory kf = null;
    try {
      kf = KeyFactory.getInstance("RSA");
    } catch (NoSuchAlgorithmException e) {
      log.fatal("KeyFactory.getInstance(String) throws NoSuchAlgorithmException, code needs to be changed!");
      e.printStackTrace();
    }

    //noinspection ConstantConditions
    return kf.generatePublic(X509publicKey);
  }

}