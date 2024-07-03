package mod.stf.syconn.api.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class SkinGrabber {

    public static void main(String[] args) {
        JsonObject jsonObject = (JsonObject) new JsonParser().parse("{\"name\":\"Syconn\",\"id\":\"5534886518c54da08b5f8eee49147499\"}");
        System.out.println(jsonObject.get("id"));
    }

    public static String convertUsernameToUUID(String name){
        try {
            HttpGet request = new HttpGet("https://api.mojang.com/users/profiles/minecraft/" + name);
            CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            JsonObject jsonObject = (JsonObject) new JsonParser().parse(EntityUtils.toString(entity));

            if (jsonObject.has("id")){
                return jsonObject.get("id").getAsString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String getTextureURL(String name){
        String id = convertUsernameToUUID(name);
        if (!id.equals("")) {
            try {
                HttpGet request = new HttpGet("https://sessionserver.mojang.com/session/minecraft/profile/" + id);
                CloseableHttpClient client = HttpClients.createDefault();
                CloseableHttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();
                JsonObject jsonObject = (JsonObject) new JsonParser().parse(EntityUtils.toString(entity));
                if (jsonObject != null){
                    String bitcode = jsonObject.getAsJsonArray("properties").get(0).getAsJsonObject().get("value").getAsString();
                    byte[] decodedBytes = Base64.decodeBase64(bitcode.getBytes());
                    JsonObject SkinData = (JsonObject) new JsonParser().parse(new String(decodedBytes));
                    return SkinData.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static NativeImage getSkinTexture(String url){
        try {
            HttpGet request = new HttpGet(url);
            CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            return NativeImage.read(entity.getContent());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean getModelType(String name){
        String id = convertUsernameToUUID(name);
        if (!id.equals("")) {
            try {
                HttpGet request = new HttpGet("https://sessionserver.mojang.com/session/minecraft/profile/" + id);
                CloseableHttpClient client = HttpClients.createDefault();
                CloseableHttpResponse response = client.execute(request);
                HttpEntity entity = response.getEntity();
                JsonObject jsonObject = (JsonObject) new JsonParser().parse(EntityUtils.toString(entity));

                if (jsonObject != null){
                    String bitcode = jsonObject.getAsJsonArray("properties").get(0).getAsJsonObject().get("value").getAsString();
                    byte[] decodedBytes = Base64.decodeBase64(bitcode.getBytes());
                    JsonObject SkinData = (JsonObject) new JsonParser().parse(new String(decodedBytes));
                    return SkinData.getAsJsonObject("textures").getAsJsonObject("SKIN").has("metadata");
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
